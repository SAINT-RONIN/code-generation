import { ref } from 'vue'

const toastMsg = ref('')
let timer = null

export function useToast() {
  return (msg) => {
    toastMsg.value = msg
    clearTimeout(timer)
    timer = setTimeout(() => { toastMsg.value = '' }, 2200)
  }
}

export { toastMsg }
