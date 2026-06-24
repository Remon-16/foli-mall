<template>
  <div class="message-list">
    <h2>{{ t('message.title') }}</h2>

    <a-spin :spinning="loading">
      <a-empty v-if="!loading && conversations.length === 0" :description="t('message.noMessages')" />

      <a-list
        v-else
        :data-source="conversations"
        item-layout="horizontal"
      >
        <template #renderItem="{ item }">
          <a-list-item class="conversation-item" @click="goToConversation(item.conversationId)">
            <a-list-item-meta>
              <template #avatar>
                <a-badge :count="item.unreadCount" :number-style="{ backgroundColor: '#f5222d' }">
                  <a-avatar :size="48">{{ item.otherUserName?.charAt(0)?.toUpperCase() }}</a-avatar>
                </a-badge>
              </template>
              <template #title>
                <span class="conv-name">{{ item.otherUserName }}</span>
                <span class="conv-time">{{ item.lastMessageTime }}</span>
              </template>
              <template #description>
                <span class="conv-last-msg">{{ item.lastMessage }}</span>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import service from '@/api'
import type { ConversationVO } from '@/types'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const conversations = ref<ConversationVO[]>([])

async function fetchConversations() {
  loading.value = true
  try {
    const res = await service.get('/messages/conversations')
    conversations.value = res.data.data as ConversationVO[]
  } finally {
    loading.value = false
  }
}

function goToConversation(conversationId: string) {
  router.push(`/messages/${conversationId}`)
}

onMounted(() => {
  fetchConversations()
})
</script>

<style scoped>
.message-list {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}
.message-list h2 {
  margin-bottom: 24px;
}
.conversation-item {
  cursor: pointer;
  padding: 16px;
  border-radius: 8px;
  transition: background-color 0.2s;
}
.conversation-item:hover {
  background-color: #fafafa;
}
.conv-name {
  font-weight: 600;
}
.conv-time {
  float: right;
  color: #999;
  font-size: 12px;
}
.conv-last-msg {
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 500px;
  display: inline-block;
}
</style>
