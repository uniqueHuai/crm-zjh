<template>
  <div class="pipeline-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">商机看板</h2>
        <p class="page-subtitle">可视化销售管道，拖拽管理商机阶段</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增商机</el-button>
      </div>
    </div>

    <!-- Summary Row -->
    <div class="summary-row">
      <div class="summary-item">
        <span class="summary-value">{{ totalOpportunities }}</span>
        <span class="summary-label">全部商机</span>
      </div>
      <div class="summary-item">
        <span class="summary-value" style="color:var(--crm-primary)">¥{{ totalAmount.toLocaleString() }}</span>
        <span class="summary-label">总金额</span>
      </div>
      <div class="summary-item">
        <span class="summary-value" style="color:var(--crm-success)">{{ winCount }}</span>
        <span class="summary-label">已赢单</span>
      </div>
      <div class="summary-item">
        <span class="summary-value" style="color:var(--crm-warning)">{{ winRate }}%</span>
        <span class="summary-label">赢单率</span>
      </div>
    </div>

    <!-- Pipeline Board -->
    <div class="pipeline-board">
      <div class="pipeline-columns">
        <div v-for="stage in stages" :key="stage.id" class="pipeline-column">
          <div class="pipeline-column-header">
            <div class="pch-left">
              <span class="pch-name">{{ stage.name }}</span>
              <el-tag size="small" :type="stage.category === 'win' ? 'success' : stage.category === 'lose' ? 'danger' : 'info'" effect="plain">
                {{ stage.opportunities.length }}
              </el-tag>
            </div>
            <span class="pch-amount">¥{{ stage.totalAmount.toLocaleString() }}</span>
          </div>
          <div class="pipeline-column-body">
            <draggable
              v-model="stage.opportunities"
              group="opportunities"
              :animation="200"
              ghost-class="ghost"
              item-key="id"
              class="pipeline-draggable"
              @change="onDragChange($event, stage)"
            >
              <template #item="{ element }">
                <div class="pipeline-card" @click="handleDetail(element)">
                  <div class="pipeline-card-top">
                    <span class="pipeline-card-title">{{ element.name }}</span>
                  </div>
                  <div class="pipeline-card-amount">¥{{ (element.expectedAmount || 0).toLocaleString() }}</div>
                  <div class="pipeline-card-progress">
                    <el-progress :percentage="element.probability || 0" :stroke-width="4" :show-text="false" />
                  </div>
                  <div class="pipeline-card-footer">
                    <span class="pipeline-card-customer">{{ element.customerName }}</span>
                    <span class="pipeline-card-owner">{{ element.ownerName }}</span>
                  </div>
                </div>
              </template>
            </draggable>
            <div v-if="!stage.opportunities.length" class="pipeline-empty">
              <el-empty description="暂无商机" :image-size="50" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Form Dialog -->
    <FormDialog v-model:visible="dialogVisible" title="新增商机" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="商机名称" prop="name" :rules="[{required:true,message:'请输入名称'}]">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="客户" prop="customerId" :rules="[{required:true,message:'请选择客户'}]">
          <el-select v-model="form.customerId" filterable style="width:100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="预计金额">
              <el-input-number v-model="form.expectedAmount" :min="0" :step="10000" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计成交日">
              <el-date-picker v-model="form.expectedCloseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="阶段">
          <el-select v-model="form.stageId" style="width:100%">
            <el-option v-for="s in stageDefs" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import draggable from 'vuedraggable'
import { getOpportunityPipeline, getOpportunityStages, createOpportunity, changeOpportunityStage, type OpportunityStage } from '@/api/modules/sales'
import { getCustomerPage, type Customer } from '@/api/modules/customer'

interface Opportunity {
  id: number; name: string; customerName: string; ownerName: string;
  expectedAmount: number; expectedCloseDate: string;
  probability: number; stageId?: number; customerId?: number;
}

interface PipelineStage {
  id: number; name: string; category: 'open' | 'win' | 'lose';
  totalAmount: number; opportunities: Opportunity[];
}

const dialogVisible = ref(false)
const stageDefs = ref<OpportunityStage[]>([])
const stages = ref<PipelineStage[]>([])
const customerOptions = ref<{ id: number; name: string }[]>([])

const totalOpportunities = computed(() => stages.value.reduce((s, st) => s + st.opportunities.length, 0))
const totalAmount = computed(() => stages.value.reduce((s, st) => s + st.totalAmount, 0))
const winCount = computed(() => stages.value.reduce((s, st) => s + (st.category === 'win' ? st.opportunities.length : 0), 0))
const winRate = computed(() => totalOpportunities.value ? Math.round(winCount.value / totalOpportunities.value * 100) : 0)

async function onDragChange(evt: any, stage: PipelineStage) {
  stage.totalAmount = stage.opportunities.reduce((s, o) => s + (o.expectedAmount || 0), 0)
  if (evt.added) {
    const opp = evt.added.element as Opportunity
    const stageDef = stageDefs.value.find(s => s.id === stage.id)
    if (stageDef) opp.probability = stageDef.probability
    try {
      await changeOpportunityStage(opp.id, stage.id)
    } catch {
      ElMessage.error('阶段更新失败')
    }
  }
}

function handleDetail(item: Opportunity) {
  ElMessage.info(`查看商机详情：${item.name}`)
}

function openDialog() {
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    await createOpportunity(formData)
    ElMessage.success('新增成功')
    done()
    await fetchPipeline()
  } catch {
    ElMessage.error('新增失败')
  }
}

async function fetchPipeline() {
  try {
    const [pipeRes, stageRes] = await Promise.all([
      getOpportunityPipeline(),
      getOpportunityStages()
    ])
    stageDefs.value = stageRes.data || []

    const pipeData = Array.isArray(pipeRes.data) ? pipeRes.data : []
    stages.value = stageDefs.value.map(def => {
      const pipe = pipeData.find((p: any) => p.stageId === def.id)
      return {
        id: def.id,
        name: def.name,
        category: def.category as 'open' | 'win' | 'lose',
        totalAmount: pipe?.totalAmount || 0,
        opportunities: (pipe?.opportunities || []).map((o: any) => ({
          ...o,
          probability: o.probability ?? def.probability,
        })),
      }
    })
  } catch {
    ElMessage.error('加载看板数据失败')
  }
}

onMounted(async () => {
  fetchPipeline()
  getCustomerPage({ page: 1, size: 999 }).then(res => {
    customerOptions.value = (res.data?.records || []).map((c: Customer) => ({ id: c.id, name: c.name }))
  })
})
</script>

<style scoped lang="scss">
.pipeline-page { max-width: 1400px; }

.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px;
  .page-title { margin:0; font-size:20px; font-weight:700; color:var(--crm-text-primary); }
  .page-subtitle { margin:4px 0 0; font-size:13px; color:var(--crm-text-secondary); }
  &-right { display:flex; gap:8px; flex-shrink:0; }
}

.summary-row { display:flex; gap:16px; margin-bottom:20px; }
.summary-item { flex:1; background:var(--crm-bg-white); border:1px solid var(--crm-border); border-radius:var(--crm-radius-lg); padding:16px 20px; display:flex; flex-direction:column; gap:4px;
  .summary-value { font-size:22px; font-weight:700; color:var(--crm-text-primary); }
  .summary-label { font-size:12px; color:var(--crm-text-secondary); }
}

.pipeline-board { overflow-x:auto; padding-bottom:8px; }
.pipeline-columns { display:flex; gap:16px; min-height:500px; align-items:stretch; }
.pipeline-column {
  flex:1; min-width:240px; max-width:300px;
  background:var(--crm-bg-gray); border-radius:var(--crm-radius-lg);
  display:flex; flex-direction:column;
  border:1px solid var(--crm-border-light);
}
.pipeline-column-header {
  display:flex; align-items:center; justify-content:space-between;
  padding:14px 16px; border-bottom:1px solid var(--crm-border-light);
  .pch-left { display:flex; align-items:center; gap:8px; }
  .pch-name { font-size:13px; font-weight:600; color:var(--crm-text-primary); }
  .pch-amount { font-size:12px; color:var(--crm-text-secondary); font-weight:500; }
}
.pipeline-column-body { flex:1; padding:8px; overflow-y:auto; }
.pipeline-draggable { min-height:60px; }
.pipeline-empty { padding:20px 0; }

.pipeline-card {
  background:var(--crm-bg-white); border-radius:var(--crm-radius-md);
  padding:14px; margin-bottom:8px; box-shadow:var(--crm-shadow-sm);
  cursor:pointer; transition:all var(--crm-transition-fast);
  border:1px solid var(--crm-border);
  &:hover { box-shadow:var(--crm-shadow-md); transform:translateY(-1px); }
}
.pipeline-card-top { display:flex; align-items:flex-start; justify-content:space-between; gap:4px; margin-bottom:6px; }
.pipeline-card-title { font-weight:600; font-size:13px; color:var(--crm-text-primary); word-break:break-all; }
.pipeline-card-amount { font-size:15px; color:var(--crm-primary); font-weight:700; margin-bottom:8px; }
.pipeline-card-progress { margin-bottom:8px; }
.pipeline-card-footer { display:flex; justify-content:space-between; font-size:12px; color:var(--crm-text-secondary); }

.ghost { opacity:0.4; }
</style>
