export const PLAN_QUICK_TAGS = [
  '制定下周计划',
  '制定本周计划',
  '制定减脂周计划',
  '制定增肌周计划'
]

export function isWeeklyPlanIntent(text = '') {
  const value = String(text).trim()
  if (!value) return false
  const hasWeek = /(本周|下周|周计划|一周|七天|7天|每周|下个星期|这个星期)/.test(value)
  const hasPlanAction = /(制定|生成|安排|规划|创建|设计|做一个|出一个)/.test(value)
  const hasTraining = /(计划|方案|训练|运动|跑步|力量|减脂|增肌|塑形|恢复|课程)/.test(value)
  return hasWeek || (hasPlanAction && hasTraining && /(计划|方案)/.test(value))
}

export function buildAiSystemPrompt(statsData = {}, userText = '') {
  return isWeeklyPlanIntent(userText)
    ? buildWeeklyPlanPrompt(statsData, userText)
    : buildGeneralPrompt(statsData)
}

function buildStatsContext(statsData = {}) {
  const s = statsData || {}
  const summary = s.summary || {}
  const today = s.todayStats || {}
  const trend = s.trendData || {}
  const recordSummary = s.recordSummary || null
  const totalDist = trend.dates?.length
    ? (trend.distances || []).reduce((a, b) => a + b, 0)
    : 0
  const totalDur = trend.dates?.length
    ? (trend.durations || []).reduce((a, b) => a + b, 0)
    : 0
  const totalCal = trend.dates?.length
    ? (trend.calories || []).reduce((a, b) => a + b, 0)
    : 0
  const typeMap = { 1: '跑步', 2: '健走', 3: '骑行' }
  const typeLines = recordSummary
    ? Object.entries(recordSummary.typeCount || {})
        .map(([type, count]) => `${typeMap[type] || '运动'}：${count} 次`)
        .join('\n')
    : ''
  const healthLines = buildHealthContext()

  return `【用户运动数据】
总运动次数：${summary.totalWorkouts || 0} 次
总距离：${summary.totalDistanceStr || '0km'}
总消耗：${summary.totalCaloriesStr || '0千卡'}
连续运动天数：${summary.streakDays || 0} 天
本周运动：${summary.weeklyWorkouts || 0} 次
本月运动：${summary.monthlyWorkouts || 0} 次
${today.statDate ? `【今日运动】
距离：${today.distanceStr || '0m'}
时长：${today.durationStr || '0分'}
消耗：${today.caloriesStr || '0千卡'}
次数：${today.recordCount || 0} 次
` : ''}${trend.dates?.length ? `【近期趋势】
统计周期：${trend.dates.length} 个数据点
周期总距离：${totalDist} 米
周期总时长：${totalDur} 秒
周期总消耗：${totalCal} 千卡
` : ''}${recordSummary ? `【记录详情】
总距离：${(recordSummary.totalDistance / 1000).toFixed(2)} km
总时长：${Math.floor(recordSummary.totalDuration / 60)} 分钟
总消耗：${recordSummary.totalCalories} 千卡
${typeLines}
` : ''}${healthLines ? `【个人健康资料】
${healthLines}
` : ''}`
}

function buildHealthContext() {
  const userInfo = getStoredUserInfo()
  const lines = [
    userInfo.gender !== undefined ? `性别：${formatGender(userInfo.gender)}` : '',
    userInfo.age ? `年龄：${userInfo.age} 岁` : '',
    userInfo.height ? `身高：${userInfo.height} cm` : '',
    userInfo.weight ? `体重：${userInfo.weight} kg` : '',
    userInfo.waistCircumference ? `腰围：${userInfo.waistCircumference} cm` : '',
    userInfo.systolicPressure && userInfo.diastolicPressure ? `血压：${userInfo.systolicPressure}/${userInfo.diastolicPressure} mmHg` : '',
    userInfo.restingHeartRate ? `静息心率：${userInfo.restingHeartRate} 次/分` : '',
    userInfo.avgExerciseHeartRate ? `运动平均心率：${userInfo.avgExerciseHeartRate} 次/分` : '',
    userInfo.maxHeartRate ? `运动最高心率：${userInfo.maxHeartRate} 次/分` : ''
  ]
  return lines.filter(Boolean).join('\n')
}

function getStoredUserInfo() {
  try {
    return uni.getStorageSync('userInfo') || {}
  } catch (e) {
    return {}
  }
}

function formatGender(gender) {
  const value = Number(gender)
  if (value === 1) return '男'
  if (value === 2) return '女'
  return '未设置'
}

function buildGeneralPrompt(statsData = {}) {
  return `你是一位专业的运动健身 AI 教练，擅长根据用户的运动数据制定个性化的训练方案。请用中文回复，语气亲切专业。
${buildStatsContext(statsData)}
请根据以上数据，为用户提供专业、可行的运动方案或建议。
【输出格式要求】
- 用中文序号做层级分隔
- 用空行分隔不同段落，保持排版整洁
- 禁止使用 Markdown 语法
- 禁止使用 表情符号
- 如果用户明确要求生成训练计划，必须追加 <<<PLAN_JSON>>> 和 <<<PLAN_JSON_END>>> 包裹的 JSON 数据
- 如果用户只是咨询建议或分析数据，不要输出 JSON`
}

function inferTotalWeeks(userText = '') {
  if (/(四周|4周|一个月|月计划)/.test(userText)) return 4
  if (/(两周|2周|双周)/.test(userText)) return 2
  return 1
}

function buildWeeklyPlanPrompt(statsData = {}, userText = '') {
  const totalWeeks = inferTotalWeeks(userText)
  return `你是一位专业的运动健身 AI 教练。当前用户请求与本周、下周或周训练计划有关，你必须使用“周计划生成模式”回复。
${buildStatsContext(statsData)}
【用户原始需求】
${userText}

【核心任务】
为用户制定一个可执行的周训练计划。计划需要能直接被应用保存，所以必须同时输出正文和结构化 JSON。

【正文要求】
1. 先给出计划名称、目标和整体安排。
2. 按周一到周日说明每天练什么、练多久、为什么这样安排。
3. 说明休息日或恢复日的目的。
4. 文本要自然、清楚、适合直接展示给用户。
5. 禁止使用 Markdown 语法，禁止使用表情符号。

【一键录入 JSON 要求】
1. 正文结束后，必须另起一行输出 <<<PLAN_JSON>>>。
2. 然后输出一个可被 JSON.parse 解析的纯 JSON 对象。
3. JSON 结束后，必须另起一行输出 <<<PLAN_JSON_END>>>。
4. JSON 中不要写注释，不要出现尾随逗号，不要使用代码块。
5. totalWeeks 默认使用 ${totalWeeks}，除非用户明确要求其他周数。
6. 一周计划必须覆盖 week=1 的 day=1 到 day=7；休息日也要生成一条 type=6、duration=0、exercises=[] 的课程，方便应用完整录入。
7. 每个非休息训练日至少包含 1 个课程，每个课程的 exercises 至少 1 个动作或步骤。

JSON 格式必须严格如下：
{
  "name": "计划名称",
  "description": "计划简介",
  "totalWeeks": ${totalWeeks},
  "level": 2,
  "courses": [
    {
      "week": 1,
      "day": 1,
      "name": "课程名称",
      "type": 1,
      "duration": 45,
      "description": "课程描述",
      "exercises": [
        {
          "name": "动作名称",
          "type": 1,
          "sets": 1,
          "reps": 0,
          "duration": 600,
          "distance": 1000,
          "restTime": 60,
          "description": "动作说明"
        }
      ]
    }
  ]
}

字段约束：
- course.type: 1=有氧，2=力量，3=拉伸，4=HIIT，5=综合，6=休息
- exercise.type: 1=有氧，2=力量，3=拉伸
- level: 1=入门，2=初级，3=中级，4=高级，5=精英
- duration: 课程用分钟，动作步骤用秒
- distance: 米，不要写公里字符串
- day: 1=周一，2=周二，3=周三，4=周四，5=周五，6=周六，7=周日
- 所有数字字段必须是数字，不能是字符串
- 休息日 exercises 必须是空数组`
}
