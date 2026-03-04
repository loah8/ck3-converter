<template>
    <!-- copy button -->
    <div class="w-full bg-gray-50 rounded-lg pb-10 h-full group">
        <div class="text-right mb-2">
          <span class="pr-4" v-if="showMsg">Copied to Clipboard</span>
          <button class="bg-transparent hover:bg-blue-500 text-blue-700 text-sm
          hover:text-white py-1 px-2 border border-blue-500 hover:border-transparent rounded"
          @click = "copy">
            Copy
          </button>
        </div>
        <textarea class="w-full h-full px-3 py-2 text-gray-700 bg-gray-50 focus:outline-none" v-model="textData"></textarea>
      </div>
</template>

<script>

export default {
  name: "CopiableTextArea",
  components: {},
  data() {
    return {
      textData: "",
      showMsg: false
    }
  },
  methods: {
    copy() {
      const clipboardData =
          event.clipboardData ||
          window.clipboardData ||
          event.originalEvent?.clipboardData ||
          navigator.clipboard;

        clipboardData.writeText(this.textData);
        this.showMsg = !this.showMsg
        console.log("DONE")
    }
  }

  }
</script>

<style scoped>

</style>