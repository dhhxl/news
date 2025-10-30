<template>
  <section id="trending" class="py-32 px-6 bg-black text-white">
    <div class="max-w-7xl mx-auto">
      <div class="text-center">
        <h2 class="text-5xl md:text-7xl font-bold mb-6">
          热门话题
        </h2>
        <p class="text-xl md:text-2xl mb-12 opacity-80">
          探索当下最受关注的新闻话题和社会热点
        </p>

        <router-link
          to="/user/news"
          class="inline-flex items-center gap-3 bg-white text-black px-8 py-4 rounded-full text-lg font-semibold hover:bg-opacity-90 transition-all hover:scale-105 hover:translate-x-2"
        >
          查看更多
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="5" y1="12" x2="19" y2="12"></line>
            <polyline points="12 5 19 12 12 19"></polyline>
          </svg>
        </router-link>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
</script>




