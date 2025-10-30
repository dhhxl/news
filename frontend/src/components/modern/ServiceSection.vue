<template>
  <section id="categories" class="py-32 px-6 bg-white">
    <div class="max-w-7xl mx-auto">
      <!-- 标题 -->
      <div class="text-center mb-20 animate-fade-in">
        <h2 class="text-5xl md:text-7xl font-bold mb-6">
          每条新闻都有温度
        </h2>
        <p class="text-lg md:text-xl max-w-3xl mx-auto leading-relaxed">
          好的新闻不仅传递信息，更能触动人心。
          及时的报道、深入的分析、多元的视角，
          让每一次阅读都能获得新的认知和思考。
        </p>
      </div>

      <!-- 分类标题 -->
      <div class="text-center mb-20">
        <h3 class="text-4xl md:text-5xl font-bold mb-4">新闻分类</h3>
        <p class="text-lg md:text-xl opacity-70">
          全方位覆盖，多角度呈现，为您提供最全面的资讯服务。
        </p>
      </div>

      <!-- 翻转卡片网格 -->
      <div class="grid md:grid-cols-3 gap-8">
        <FlipCard
          v-for="(service, index) in services"
          :key="service.title"
          :service="service"
          :index="index"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
interface ServiceItem {
  name: string
  desc: string
}

interface Service {
  title: string
  iconComponent: string
  iconColor: string
  gradientFrom: string
  gradientTo: string
  description: string
  items: ServiceItem[]
}

const services: Service[] = [
  {
    title: '实时新闻',
    iconComponent: 'Reading',
    iconColor: '#ffffff',
    gradientFrom: '#000000',
    gradientTo: '#1a1a1a',
    description: '24小时不间断追踪，第一时间了解全球动态',
    items: [
      { name: '突发新闻', desc: '实时推送重大突发事件' },
      { name: '滚动播报', desc: '持续更新最新进展' },
      { name: '快讯速递', desc: '简明扼要传递要点' },
      { name: '视频直播', desc: '现场直击新闻现场' },
      { name: '图片报道', desc: '高清图片全景呈现' },
      { name: '数据可视化', desc: '图表解读复杂信息' }
    ]
  },
  {
    title: '深度报道',
    iconComponent: 'DataAnalysis',
    iconColor: '#ffffff',
    gradientFrom: '#1a1a1a',
    gradientTo: '#333333',
    description: '专业记者深入调查，提供有深度的新闻解读',
    items: [
      { name: '调查报道', desc: '深入挖掘事件真相' },
      { name: '专题策划', desc: '系统梳理热点话题' },
      { name: '人物专访', desc: '对话新闻当事人' },
      { name: '观察评论', desc: '资深编辑独到见解' },
      { name: '数据分析', desc: '用数据说话讲故事' },
      { name: '背景解读', desc: '还原事件来龙去脉' }
    ]
  },
  {
    title: '互动社区',
    iconComponent: 'UserFilled',
    iconColor: '#ffffff',
    gradientFrom: '#333333',
    gradientTo: '#4a4a4a',
    description: '打造活跃的新闻讨论社区，分享观点交流见解',
    items: [
      { name: '读者评论', desc: '发表个人观点看法' },
      { name: '话题讨论', desc: '参与热门话题互动' },
      { name: '爆料投稿', desc: '提供新闻线索' },
      { name: '记者连线', desc: '与记者实时互动' },
      { name: '民意调查', desc: '参与问卷投票' },
      { name: '用户原创', desc: '分享你的故事' }
    ]
  }
]
</script>




