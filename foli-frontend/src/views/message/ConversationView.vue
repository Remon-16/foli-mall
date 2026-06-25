<template>
  <div class="conversation-view">
    <div class="chat-header">
      <a-button type="text" @click="$router.push('/messages')">
        &larr; {{ t('common.back') }}
      </a-button>
      <span class="chat-title">{{ otherUserName }}</span>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <a-spin :spinning="loading">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-bubble"
          :class="msg.senderId === currentUserId ? 'sent' : 'received'"
        >
          <div class="bubble-content">
            <div class="bubble-sender">{{ msg.senderName }}</div>
            <div class="bubble-text">{{ msg.content }}</div>
            <div class="bubble-time">{{ msg.createTime }}</div>
          </div>
        </div>
        <a-empty v-if="!loading && messages.length === 0" :description="t('message.noMessages')" />
      </a-spin>
    </div>

    <div class="chat-input">
      <a-input
        v-model:value="inputMessage"
        :placeholder="t('message.inputPlaceholder')"
        @pressEnter="sendMessage"
      />
      <a-button type="primary" @click="sendMessage" :disabled="!inputMessage.trim()">
        {{ t('message.sendMessage') }}
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import service from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { MessageVO, ConversationVO } from '@/types'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const messages = ref<MessageVO[]>([])
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const currentUserId = computed(() => authStore.userInfo?.id ?? '')
const otherUserName = ref('')
const receiverId = ref<string>('')
const isNewConversation = ref(false)

async function fetchMessages() {
  const conversationId = route.params.conversationId as string
  loading.value = true
  try {
    const res = await service.get(`/messages/conversation/${conversationId}`, {
      params: { page: 1, pageSize: 50 },
    })
    const data = res.data.data
    if (Array.isArray(data)) {
      messages.value = data as MessageVO[]
    } else if (data && (data as any).records) {
      messages.value = (data as any).records as MessageVO[]
    }

    if (messages.value.length > 0) {
      const lastMsg = messages.value[messages.value.length - 1]
      if (lastMsg.senderId === currentUserId.value) {
        receiverId.value = lastMsg.receiverId
        otherUserName.value = lastMsg.receiverName
      } else {
        receiverId.value = lastMsg.senderId
        otherUserName.value = lastMsg.senderName
      }
    }
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function sendMessage() {
  const content = inputMessage.value.trim()
  if (!content) return
  if (!receiverId.value) {
    message.warning('Unknown receiver')
    return
  }
  try {
    const res = await service.post('/messages', { receiverId: receiverId.value, content })
    inputMessage.value = ''
    if (isNewConversation.value) {
      const sent = res.data.data as MessageVO
      if (sent.conversationId) {
        isNewConversation.value = false
        await router.replace(`/messages/${sent.conversationId}`)
        await fetchMessages()
        await nextTick()
        scrollToBottom()
        return
      }
    }
    await fetchMessages()
    await nextTick()
    scrollToBottom()
  } catch {
    // interceptor handles error
  }
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

watch(messages, async () => {
  await nextTick()
  scrollToBottom()
})

onMounted(async () => {
  const queryReceiverId = route.query.receiverId as string | undefined
  const queryReceiverName = route.query.receiverName as string | undefined

  if (queryReceiverId) {
    isNewConversation.value = true
    receiverId.value = queryReceiverId
    otherUserName.value = queryReceiverName || ''
  } else {
    await fetchMessages()
    await nextTick()
    scrollToBottom()
  }
})
</script>

<style scoped>
.conversation-view {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px);
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
}
.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}
.chat-title {
  font-size: 18px;
  font-weight: 600;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
}
.message-bubble {
  display: flex;
  margin-bottom: 16px;
}
.message-bubble.sent {
  justify-content: flex-end;
}
.message-bubble.received {
  justify-content: flex-start;
}
.bubble-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
}
.sent .bubble-content {
  background-color: #1677ff;
  color: #fff;
}
.received .bubble-content {
  background-color: #f0f0f0;
}
.bubble-sender {
  font-size: 12px;
  margin-bottom: 4px;
  opacity: 0.7;
}
.bubble-text {
  word-break: break-word;
}
.bubble-time {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.6;
}
.chat-input {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
}
.chat-input :first-child {
  flex: 1;
}
</style>
