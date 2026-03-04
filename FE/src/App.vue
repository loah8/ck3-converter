<template>
  <div class="h-full">
    <Converter/>
  </div>
</template>

<script>
import Converter from "./components/Converter";
import CopiableTextArea from "./components/CopiableTextArea";

export default {
  name: 'App',
  components: {
    CopiableTextArea,
    Converter
  }
}
</script>

<style>

html, body {
  height: 100%;
}

</style>
