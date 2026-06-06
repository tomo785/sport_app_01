<template>
  <u-icon
    :name="iconName"
    :color="color"
    :size="size"
    :bold="bold"
    :custom-style="normalizedStyle"
  />
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({
  name: {
    type: String,
    default: 'info'
  },
  color: {
    type: String,
    default: 'currentColor'
  },
  size: {
    type: [String, Number],
    default: 28
  },
  bold: {
    type: Boolean,
    default: false
  },
  customStyle: {
    type: Object,
    default: () => ({})
  }
})
const iconMap = {
  account: 'account',
  ai: 'kefu-ermai',
  achievement: 'star-fill',
  arrowDown: 'arrow-down',
  arrowLeft: 'arrow-left',
  arrowRight: 'arrow-right',
  arrowUp: 'arrow-up',
  beginner: 'level',
  calendar: 'calendar',
  cardio: 'heart-fill',
  chart: 'list-dot',
  check: 'checkmark',
  checkCircle: 'checkmark-circle-fill',
  chevronDown: 'arrow-down',
  chevronLeft: 'arrow-left',
  chevronRight: 'arrow-right',
  chevronUp: 'arrow-up',
  close: 'close',
  copy: 'file-text',
  cycling: 'play-right-fill',
  day: 'clock-fill',
  delete: 'trash',
  edit: 'edit-pen',
  empty: 'empty-data',
  goal: 'scan',
  guest: 'coupon',
  hiit: 'warning-fill',
  hydration: 'plus-circle-fill',
  info: 'info-circle',
  intensity: 'warning-fill',
  location: 'map-fill',
  lock: 'lock-fill',
  map: 'map-fill',
  menu: 'more-dot-fill',
  night: 'clock',
  plan: 'file-text',
  rest: 'pause-circle-fill',
  run: 'play-circle-fill',
  save: 'checkmark-circle',
  search: 'search',
  setting: 'setting',
  share: 'share-square',
  shuffle: 'reload',
  steps: 'play-right',
  strength: 'plus-circle-fill',
  stretch: 'heart',
  target: 'scan',
  theme: 'photo',
  time: 'clock',
  upload: 'share-square',
  users: 'plus-people-fill',
  views: 'eye-fill',
  walk: 'play-right',
  warning: 'warning-fill',
  woman: 'woman',
  man: 'man'
}
const iconName = computed(() => iconMap[props.name] || props.name || 'info-circle')
const normalizedStyle = computed(() => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  ...props.customStyle
}))
</script>
