<template>
  <div class="code-editor">
    <div ref="editorContainer" class="editor-container"></div>
  </div>
</template>

<script>
import * as monaco from 'monaco-editor'

export default {
  name: 'CodeEditor',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    language: {
      type: String,
      default: 'cpp'
    }
  },
  emits: ['update:modelValue'],
  data() {
    return {
      editor: null
    }
  },
  mounted() {
    this.initEditor()
  },
  beforeUnmount() {
    if (this.editor) {
      this.editor.dispose()
    }
  },
  watch: {
    modelValue(newValue) {
      if (this.editor && this.editor.getValue() !== newValue) {
        this.editor.setValue(newValue)
      }
    },
    language(newValue) {
      if (this.editor) {
        monaco.editor.setModelLanguage(this.editor.getModel(), newValue)
      }
    }
  },
  methods: {
    initEditor() {
      this.editor = monaco.editor.create(this.$refs.editorContainer, {
        value: this.modelValue,
        language: this.language,
        theme: 'vs',
        automaticLayout: true,
        minimap: {
          enabled: true
        },
        scrollBeyondLastLine: false,
        fontSize: 14,
        lineNumbers: 'on',
        scrollbar: {
          vertical: 'auto',
          horizontal: 'auto'
        }
      })
      
      this.editor.onDidChangeModelContent(() => {
        const value = this.editor.getValue()
        this.$emit('update:modelValue', value)
      })
    }
  }
}
</script>

<style scoped>
.code-editor {
  width: 100%;
  height: 100%;
}

.editor-container {
  width: 100%;
  height: 400px;
  border: 1px solid #ddd;
  border-radius: 0.5rem;
  overflow: hidden;
}
</style>