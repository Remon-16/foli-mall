<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import service from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { BalanceLogVO, PageResult } from '@/types'

const { t } = useI18n()
const authStore = useAuthStore()

const balance = ref(0)
const rechargeAmount = ref(100)
const recharging = ref(false)
const loadingLogs = ref(false)

const logs = ref<BalanceLogVO[]>([])
const logTotal = ref(0)
const logParams = reactive({
  page: 1,
  pageSize: 10,
})

const logColumns = [
  {
    title: () => t('account.balanceLogs'),
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: () => t('account.rechargeAmount'),
    dataIndex: 'amount',
    key: 'amount',
  },
  {
    title: 'Type',
    dataIndex: 'type',
    key: 'type',
  },
  {
    title: 'Before',
    dataIndex: 'beforeBalance',
    key: 'beforeBalance',
  },
  {
    title: 'After',
    dataIndex: 'afterBalance',
    key: 'afterBalance',
  },
  {
    title: 'Remark',
    dataIndex: 'remark',
    key: 'remark',
  },
  {
    title: 'Time',
    dataIndex: 'createTime',
    key: 'createTime',
  },
]

onMounted(() => {
  fetchBalance()
  fetchLogs()
})

async function fetchBalance() {
  try {
    const res = await service.get('/account/balance')
    balance.value = res.data.data as number
  } catch {
    // handled by interceptor
  }
}

async function handleRecharge() {
  if (rechargeAmount.value <= 0) {
    message.warning(t('account.insufficientBalance'))
    return
  }
  recharging.value = true
  try {
    await service.post('/account/recharge', {
      amount: rechargeAmount.value,
    })
    message.success(t('account.rechargeSuccess'))
    await fetchBalance()
    rechargeAmount.value = 100
    // Refresh logs after recharge
    logParams.page = 1
    await fetchLogs()
  } catch {
    // handled by interceptor
  } finally {
    recharging.value = false
  }
}

async function fetchLogs() {
  loadingLogs.value = true
  try {
    const res = await service.get('/account/balance-logs', {
      params: {
        page: logParams.page,
        pageSize: logParams.pageSize,
      },
    })
    const data = res.data.data as PageResult<BalanceLogVO>
    logs.value = data.records
    logTotal.value = data.total
  } catch {
    // handled by interceptor
  } finally {
    loadingLogs.value = false
  }
}

function handleLogPageChange(page: number) {
  logParams.page = page
  fetchLogs()
}
</script>

<template>
  <div class="account-container">
    <!-- Balance Card -->
    <a-card :bordered="false" class="balance-card">
      <a-row align="middle" :gutter="[24, 16]">
        <a-col :xs="24" :md="8">
          <div class="balance-display">
            <span class="balance-label">{{ t('account.balance') }}:</span>
            <span class="balance-value">¥{{ balance.toFixed(2) }}</span>
          </div>
        </a-col>
        <a-col :xs="24" :md="16">
          <a-space :size="12">
            <a-input-number
              v-model:value="rechargeAmount"
              :min="1"
              :step="100"
              size="large"
              style="width: 160px"
              :placeholder="t('account.rechargeAmount')"
            />
            <a-button
              type="primary"
              size="large"
              :loading="recharging"
              @click="handleRecharge"
            >
              {{ t('account.recharge') }}
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- Balance Logs Table -->
    <a-card :title="t('account.balanceLogs')" :bordered="false" class="logs-card">
      <a-spin :spinning="loadingLogs">
        <a-table
          :columns="logColumns"
          :data-source="logs"
          :pagination="false"
          row-key="id"
          :scroll="{ x: 800 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'amount'">
              <span :class="record.amount > 0 ? 'amount-positive' : 'amount-negative'">
                {{ record.amount > 0 ? '+' : '' }}{{ record.amount }}
              </span>
            </template>
            <template v-else-if="column.key === 'type'">
              <a-tag
                :color="
                  record.type === 'recharge'
                    ? 'green'
                    : record.type === 'pay'
                      ? 'red'
                      : 'blue'
                "
              >
                {{
                  record.type === 'recharge'
                    ? t('account.typeRecharge')
                    : record.type === 'pay'
                      ? t('account.typePay')
                      : t('account.typeRefund')
                }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-spin>

      <div v-if="logTotal > 0" class="log-pagination">
        <a-pagination
          :current="logParams.page"
          :total="logTotal"
          :page-size="logParams.pageSize"
          :show-total="(total: number) => t('common.total', { total })"
          @change="handleLogPageChange"
        />
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.account-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.balance-card {
  margin-bottom: 24px;
}

.balance-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.balance-label {
  font-size: 16px;
  color: #666;
}

.balance-value {
  font-size: 36px;
  font-weight: 700;
  color: #cf1322;
}

.amount-positive {
  color: #52c41a;
  font-weight: 600;
}

.amount-negative {
  color: #cf1322;
  font-weight: 600;
}

.logs-card {
  margin-bottom: 24px;
}

.log-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
