# 🔧 前端API路径修复

## 🐛 问题

前端请求 `/api/api/images/unused` - 路径重复！

**原因**：
- `request.ts` 的 baseURL：`/api`
- 前端API调用：`/api/images/...`
- 实际请求：`/api` + `/api/images/...` = `/api/api/images/...` ❌

---

## ✅ 解决方案

移除前端API调用中的 `/api` 前缀：

### 修改的文件

**`frontend/src/api/editor.ts`**:
```typescript
// 之前：
request.post('/api/images/upload', ...)
request.get('/api/images/unused')

// 之后：
request.post('/images/upload', ...)
request.get('/images/unused')
```

**`frontend/src/api/admin.ts`**:
```typescript
// 之前：
request.post('/api/images/cleanup', ...)

// 之后：
request.post('/images/cleanup', ...)
```

---

## 📊 完整路径映射

```
前端代码：request.get('/images/unused')
    ↓
request baseURL：/api
    ↓
实际请求：/api/images/unused
    ↓
后端 context-path：/api
    ↓
后端 Controller：/images
    ↓
完整路径：/api + /images/unused
    ↓
匹配 ImageUploadController ✅
```

---

## 🧪 测试

修改后需要：
1. 重新加载前端（刷新浏览器或重启dev server）
2. 测试图片上传功能
3. 测试获取未使用图片

---

**修复时间**：2025-10-28 09:20  
**影响文件**：
- `frontend/src/api/editor.ts`
- `frontend/src/api/admin.ts`

