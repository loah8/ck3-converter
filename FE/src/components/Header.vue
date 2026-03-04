<template>
  <div class="flex items-center text-white p-6 bg-pink-900">
    <span class="font-semibold text-xl tracking-tight">CK3 DNA Converter</span>
    <div>
      <span> CK3 v.{{ckVersion}} </span>
      <span> Updated : {{updatedDate}}</span>
    </div>
  </div>
</template>

<script>
import { info as versions } from '@/files/info.js'

export default {
  name: "Header",
  data() {
    return {
      versions: versions,
      updatedDate : "",
      ckVersion: ""
    }
  },
  created() {
    this.ckVersion = versions.rawVersion;
    this.updatedDate = new Date(versions.lastUpdate)
  }
}
</script>

<style scoped>

</style>