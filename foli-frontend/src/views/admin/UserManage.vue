<template>
  <div class="user-manage">
    <h2>{{ t('admin.userManagement') }}</h2>

    <div class="filters">
      <a-select
        v-model:value="roleFilter"
        :placeholder="t('admin.role')"
        style="width: 140px; margin-right: 12px"
        allow-clear
        @change="fetchUsers"
      >
        <a-select-option :value="null">{{ t('common.all') }}</a-select-option>
        <a-select-option :value="0">{{ t('admin.roleBuyer') }}</a-select-option>
        <a-select-option :value="1">{{ t('admin.roleSeller') }}</a-select-option>
        <a-select-option :value="2">{{ t('admin.roleAdmin') }}</a-select-option>
      </a-select>
      <a-select
        v-model:value="statusFilter"
        :placeholder="t('admin.userStatus')"
        style="width: 140px"
        allow-clear
        @change="fetchUsers"
      >
        <a-select-option :value="null">{{ t('common.all') }}</a-select-option>
        <a-select-option :value="1">{{ t('admin.enable') }}</a-select-option>
        <a-select-option :value="0">{{ t('admin.disable') }}</a-select-option>
      </a-select>
    </div>

    <a-table
      :columns="columns"
      :data-source="users"
      :loading="loading"
      :pagination="{ total, current: pagination.page, pageSize: pagination.pageSize, showSizeChanger: false }"
      @change="handleTableChange"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'role'">
          <a-tag :color="roleColor(record.role)">
            {{ roleText(record.role) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? t('admin.enable') : t('admin.disable') }}
          </a-tag>
        </template>
        <template v-if="column.key === 'balance'">
          &yen;{{ record.balance }}
        </template>
        <template v-if="column.key === 'actions'">
          <a-switch
            :checked="record.status === 1"
            :checked-children="t('admin.enable')"
            :un-checked-children="t('admin.disable')"
            @change="(val: boolean) => toggleUserStatus(record.id, val)"
          />
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import service from '@/api'
import type { UserInfo, PageResult } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const users = ref<UserInfo[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 10 })
const roleFilter = ref<number | null>(null)
const statusFilter = ref<number | null>(null)

const columns = [
  { title: t('auth.username'), key: 'username', dataIndex: 'username' },
  { title: t('auth.nickname'), key: 'nickname', dataIndex: 'nickname' },
  { title: t('admin.role'), key: 'role' },
  { title: t('account.balance'), key: 'balance' },
  { title: t('admin.userStatus'), key: 'status' },
  { title: t('time'), key: 'createTime', dataIndex: 'createTime' },
  { title: t('common.actions'), key: 'actions' },
]

function roleText(role: number): string {
  const map: Record<number, string> = { 0: t('admin.roleBuyer'), 1: t('admin.roleSeller'), 2: t('admin.roleAdmin') }
  return map[role] ?? ''
}

function roleColor(role: number): string {
  const colors: Record<number, string> = { 0: 'blue', 1: 'orange', 2: 'red' }
  return colors[role] ?? 'default'
}

async function fetchUsers() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (roleFilter.value !== null) params.role = roleFilter.value
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await service.get('/admin/users', { params })
    const data = res.data.data as PageResult<UserInfo>
    users.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: { current: number; pageSize: number }) {
  pagination.page = pag.current
  pagination.pageSize = pag.pageSize
  fetchUsers()
}

async function toggleUserStatus(id: number, enabled: boolean) {
  try {
    await service.put(`/admin/users/${id}/status`, null, {
      params: { status: enabled ? 1 : 0 },
    })
    message.success(t('common.success'))
    fetchUsers()
  } catch {
    // interceptor handles error
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-manage {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}
.user-manage h2 {
  margin-bottom: 24px;
}
.filters {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
</style>
