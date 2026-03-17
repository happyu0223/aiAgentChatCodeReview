<template>
  <div class="code-editor" ref="editorRef"></div>
</template>

<script setup>
import { ref, onMounted, watch, shallowRef } from 'vue'
import { EditorState } from '@codemirror/state'
import { EditorView, keymap, lineNumbers, highlightActiveLineGutter, highlightSpecialChars, drawSelection, dropCursor, rectangularSelection, crosshairCursor, highlightActiveLine } from '@codemirror/view'
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands'
import { syntaxHighlighting, defaultHighlightStyle, bracketMatching, foldGutter, indentOnInput } from '@codemirror/language'
import { java } from '@codemirror/lang-java'
import { python } from '@codemirror/lang-python'
import { javascript } from '@codemirror/lang-javascript'
import { oneDark } from '@codemirror/theme-one-dark'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  language: {
    type: String,
    default: 'java'
  },
  readonly: {
    type: Boolean,
    default: false
  },
  theme: {
    type: String,
    default: 'oneDark'
  }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
const view = shallowRef(null)

const getLanguageExtension = (lang) => {
  const map = {
    java: java(),
    python: python(),
    javascript: javascript(),
    vue: javascript(),
    go: javascript()
  }
  return map[lang] || java()
}

const createEditor = () => {
  if (view.value) {
    view.value.destroy()
  }

  const extensions = [
    lineNumbers(),
    highlightActiveLineGutter(),
    highlightSpecialChars(),
    history(),
    foldGutter(),
    drawSelection(),
    dropCursor(),
    EditorState.allowMultipleSelections.of(true),
    indentOnInput(),
    bracketMatching(),
    rectangularSelection(),
    crosshairCursor(),
    highlightActiveLine(),
    keymap.of([
      ...defaultKeymap,
      ...historyKeymap
    ]),
    syntaxHighlighting(defaultHighlightStyle, { fallback: true }),
    getLanguageExtension(props.language),
    EditorView.updateListener.of((update) => {
      if (update.docChanged) {
        emit('update:modelValue', update.state.doc.toString())
      }
    }),
    EditorView.theme({
      '&': {
        height: '350px',
        fontSize: '13px'
      },
      '.cm-scroller': {
        overflow: 'auto'
      }
    })
  ]

  if (props.theme === 'oneDark') {
    extensions.push(oneDark)
  }

  if (props.readonly) {
    extensions.push(EditorState.readOnly.of(true))
  }

  const state = EditorState.create({
    doc: props.modelValue,
    extensions
  })

  view.value = new EditorView({
    state,
    parent: editorRef.value
  })
}

onMounted(() => {
  createEditor()
})

watch(() => props.language, () => {
  createEditor()
})

watch(() => props.modelValue, (newVal) => {
  if (view.value && newVal !== view.value.state.doc.toString()) {
    view.value.dispatch({
      changes: {
        from: 0,
        to: view.value.state.doc.length,
        insert: newVal
      }
    })
  }
})
</script>

<style scoped>
.code-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.code-editor :deep(.cm-editor) {
  height: 350px;
}

.code-editor :deep(.cm-scroller) {
  font-family: 'Consolas', 'Monaco', monospace;
}
</style>
