# Tasks: 新闻管理系统

**Input**: Design documents from `/specs/001-zhipuai/`
**Prerequisites**: plan.md (✅), spec.md (✅), research.md (✅), data-model.md (✅), contracts/ (✅)

**Tests**: Tests are included to meet constitution requirements (≥80% coverage, Test-First approach)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions
- **Web app**: `backend/src/`, `frontend/src/`
- Backend: Java package structure `com.news.*`
- Frontend: Vue 3 component structure

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create root project directories: `backend/`, `frontend/`, `docker-compose.yml`
- [x] T002 [P] Initialize Spring Boot backend project with Maven in `backend/pom.xml`
- [x] T003 [P] Initialize Vue 3 frontend project with Vite in `frontend/`
- [x] T004 [P] Configure Git repository with `.gitignore` for Java and Node.js
- [x] T005 [P] Setup Docker Compose for MySQL 8.0 and Redis 7.x in `docker-compose.yml`
- [x] T006 [P] Configure backend application properties in `backend/src/main/resources/application.yml`
- [x] T007 [P] Configure frontend environment variables in `frontend/.env.development`
- [x] T008 [P] Setup Checkstyle for Java in `backend/checkstyle.xml`
- [x] T009 [P] Setup ESLint and Prettier for Vue 3 in `frontend/.eslintrc.js`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T010 Setup Flyway database migration framework in `backend/pom.xml` and `backend/src/main/resources/db/migration/`
- [x] T011 Create V1__init_schema.sql with all 7 tables (users, categories, news, summaries, classification_rules, crawl_tasks, audit_logs)
- [x] T012 Create V2__add_default_categories.sql to insert 6 default categories (政治、经济、体育、娱乐、科技、社会)
- [x] T013 Create V3__add_default_admin.sql to insert default admin user (username: admin, password: BCrypt hashed)
- [x] T014 Create V4__add_indexes.sql for performance optimization (composite indexes for sorting, fulltext search)
- [x] T015 [P] Configure Spring Security in `backend/src/main/java/com/news/config/SecurityConfig.java`
- [x] T016 [P] Implement JWT token provider in `backend/src/main/java/com/news/security/JwtTokenProvider.java`
- [x] T017 [P] Create JWT authentication filter in `backend/src/main/java/com/news/security/JwtAuthenticationFilter.java`
- [x] T018 [P] Setup Redis configuration in `backend/src/main/java/com/news/config/RedisConfig.java`
- [x] T019 [P] Create global exception handler in `backend/src/main/java/com/news/exception/GlobalExceptionHandler.java`
- [x] T020 [P] Setup CORS configuration in `backend/src/main/java/com/news/config/CorsConfig.java`
- [x] T021 [P] Configure Spring Boot Actuator for monitoring in `backend/src/main/resources/application.yml`
- [x] T022 [P] Setup Axios request interceptor for JWT in `frontend/src/utils/request.ts`
- [x] T023 [P] Create Vue Router configuration in `frontend/src/router/index.ts`
- [x] T024 [P] Setup Pinia stores structure in `frontend/src/stores/`
- [x] T025 [P] Create layout components: AdminLayout.vue and UserLayout.vue in `frontend/src/components/layout/`

**Checkpoint**: ✅ Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - 管理员登录和身份验证 (Priority: P1) 🎯 MVP

**Goal**: 实现管理员通过安全的登录系统访问新闻管理功能，包括JWT认证、会话管理和访问控制

**Independent Test**: 可以通过创建测试账户、尝试登录、验证访问权限来独立测试，无需其他功能即可验证登录系统的完整性

### Tests for User Story 1

**NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T026 [P] [US1] Create integration test for login endpoint in `backend/src/test/java/com/news/controller/AuthControllerTest.java`
- [ ] T027 [P] [US1] Create integration test for JWT token validation in `backend/src/test/java/com/news/security/JwtTokenProviderTest.java`
- [ ] T028 [P] [US1] Create E2E test for login flow in `frontend/tests/e2e/login.spec.ts` using Playwright

### Implementation for User Story 1

- [x] T029 [P] [US1] Create User entity in `backend/src/main/java/com/news/model/entity/User.java`
- [x] T030 [P] [US1] Create UserRepository in `backend/src/main/java/com/news/repository/UserRepository.java`
- [x] T031 [US1] Create CustomUserDetailsService in `backend/src/main/java/com/news/security/CustomUserDetailsService.java` (depends on T030)
- [x] T032 [US1] Create LoginRequest DTO in `backend/src/main/java/com/news/model/dto/LoginRequest.java`
- [x] T033 [US1] Create LoginResponse DTO in `backend/src/main/java/com/news/model/dto/LoginResponse.java`
- [x] T034 [US1] Create UserService in `backend/src/main/java/com/news/service/UserService.java` (depends on T030)
- [x] T035 [US1] Implement AuthController with login/logout/me endpoints in `backend/src/main/java/com/news/controller/AuthController.java` (depends on T034)
- [x] T036 [US1] Create Login.vue page in `frontend/src/views/Login.vue`
- [x] T037 [US1] Create auth API client in `frontend/src/api/auth.ts`
- [x] T038 [US1] Create user Pinia store in `frontend/src/stores/user.ts` (depends on T037)
- [x] T039 [US1] Implement login form with validation in Login.vue (depends on T036, T038)
- [x] T040 [US1] Add route guards for authentication in `frontend/src/router/index.ts`
- [x] T041 [US1] Create logout functionality and token refresh logic

**Checkpoint**: ✅ User Story 1 is fully functional - users can log in, access protected routes, and be redirected if unauthorized

---

## Phase 4: User Story 2 - 新闻内容管理 (Priority: P2)

**Goal**: 实现新闻的完整CRUD功能，管理员可以创建、编辑、删除新闻，支持搜索筛选和分类管理

**Independent Test**: 可以通过预先填充一些测试新闻数据，然后执行增删改查操作来独立测试，验证所有管理操作是否正常工作

### Tests for User Story 2

- [ ] T042 [P] [US2] Create integration test for news CRUD endpoints in `backend/src/test/java/com/news/controller/NewsControllerTest.java`
- [ ] T043 [P] [US2] Create integration test for category CRUD endpoints in `backend/src/test/java/com/news/controller/CategoryControllerTest.java`
- [ ] T044 [P] [US2] Create unit test for NewsService in `backend/src/test/java/com/news/service/NewsServiceTest.java`
- [ ] T045 [P] [US2] Create E2E test for news management in `frontend/tests/e2e/news-management.spec.ts`

### Implementation for User Story 2

- [ ] T046 [P] [US2] Create Category entity in `backend/src/main/java/com/news/model/entity/Category.java`
- [ ] T047 [P] [US2] Create News entity in `backend/src/main/java/com/news/model/entity/News.java`
- [ ] T048 [P] [US2] Create AuditLog entity in `backend/src/main/java/com/news/model/entity/AuditLog.java`
- [ ] T049 [P] [US2] Create CategoryRepository in `backend/src/main/java/com/news/repository/CategoryRepository.java`
- [ ] T050 [P] [US2] Create NewsRepository with custom queries for sorting in `backend/src/main/java/com/news/repository/NewsRepository.java`
- [ ] T051 [P] [US2] Create AuditLogRepository in `backend/src/main/java/com/news/repository/AuditLogRepository.java`
- [ ] T052 [P] [US2] Create News DTOs (NewsDTO, CreateNewsRequest, UpdateNewsRequest) in `backend/src/main/java/com/news/model/dto/`
- [ ] T053 [P] [US2] Create Category DTOs in `backend/src/main/java/com/news/model/dto/`
- [ ] T054 [US2] Implement CategoryService in `backend/src/main/java/com/news/service/CategoryService.java` (depends on T049)
- [ ] T055 [US2] Implement NewsService with CRUD and search in `backend/src/main/java/com/news/service/NewsService.java` (depends on T050, T054)
- [ ] T056 [US2] Implement AuditLogService for recording operations in `backend/src/main/java/com/news/service/AuditLogService.java` (depends on T051)
- [ ] T057 [US2] Implement smart sorting algorithm (time decay + view count) in NewsService (depends on T055)
- [ ] T058 [US2] Create NewsController with admin CRUD endpoints in `backend/src/main/java/com/news/controller/NewsController.java` (depends on T055)
- [ ] T059 [US2] Create CategoryController in `backend/src/main/java/com/news/controller/CategoryController.java` (depends on T054)
- [ ] T060 [US2] Setup Redis caching for news list in `backend/src/main/java/com/news/service/NewsService.java`
- [ ] T061 [P] [US2] Create NewsList.vue for admin in `frontend/src/views/admin/NewsList.vue`
- [ ] T062 [P] [US2] Create NewsEdit.vue for creating/editing in `frontend/src/views/admin/NewsEdit.vue`
- [ ] T063 [P] [US2] Create CategoryManage.vue in `frontend/src/views/admin/CategoryManage.vue`
- [ ] T064 [P] [US2] Create NewsListItem.vue component in `frontend/src/components/news/NewsListItem.vue`
- [ ] T065 [P] [US2] Create news API client in `frontend/src/api/news.ts`
- [ ] T066 [P] [US2] Create category API client in `frontend/src/api/category.ts`
- [ ] T067 [US2] Create news Pinia store in `frontend/src/stores/news.ts` (depends on T065)
- [ ] T068 [US2] Create category Pinia store in `frontend/src/stores/category.ts` (depends on T066)
- [ ] T069 [US2] Implement news list with search/filter UI (depends on T061, T067)
- [ ] T070 [US2] Implement news editor with rich text and validation (depends on T062, T067)
- [ ] T071 [US2] Implement category management UI (depends on T063, T068)
- [ ] T072 [US2] Add audit log viewer page in `frontend/src/views/admin/AuditLog.vue`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - admins can fully manage news and categories

---

## Phase 5: User Story 3 - 多源新闻采集 (Priority: P3)

**Goal**: 实现从新浪网、央视网、网易新闻自动采集新闻，支持定时任务和手动触发，包含自动分类和重复检测

**Independent Test**: 可以通过配置采集规则、手动触发采集任务、验证采集的新闻数据完整性来独立测试，不依赖其他用户故事

### Tests for User Story 3

- [ ] T073 [P] [US3] Create unit test for SinaCrawler in `backend/src/test/java/com/news/crawler/SinaCrawlerTest.java`
- [ ] T074 [P] [US3] Create unit test for CctvCrawler in `backend/src/test/java/com/news/crawler/CctvCrawlerTest.java`
- [ ] T075 [P] [US3] Create unit test for NeteaseCrawler in `backend/src/test/java/com/news/crawler/NeteaseCrawlerTest.java`
- [ ] T076 [P] [US3] Create integration test for CrawlerService in `backend/src/test/java/com/news/service/CrawlerServiceTest.java`
- [ ] T077 [P] [US3] Create integration test for ClassificationService in `backend/src/test/java/com/news/service/ClassificationServiceTest.java`

### Implementation for User Story 3

- [ ] T078 [P] [US3] Create ClassificationRule entity in `backend/src/main/java/com/news/model/entity/ClassificationRule.java`
- [ ] T079 [P] [US3] Create CrawlTask entity in `backend/src/main/java/com/news/model/entity/CrawlTask.java`
- [ ] T080 [P] [US3] Create ClassificationRuleRepository in `backend/src/main/java/com/news/repository/ClassificationRuleRepository.java`
- [ ] T081 [P] [US3] Create CrawlTaskRepository in `backend/src/main/java/com/news/repository/CrawlTaskRepository.java`
- [ ] T082 [US3] Implement ClassificationService with rule matching logic in `backend/src/main/java/com/news/service/ClassificationService.java` (depends on T080)
- [ ] T083 [P] [US3] Create NewsCrawler interface in `backend/src/main/java/com/news/crawler/NewsCrawler.java`
- [ ] T084 [P] [US3] Implement SinaCrawler with JSoup in `backend/src/main/java/com/news/crawler/SinaCrawler.java`
- [ ] T085 [P] [US3] Implement CctvCrawler with JSoup in `backend/src/main/java/com/news/crawler/CctvCrawler.java`
- [ ] T086 [P] [US3] Implement NeteaseCrawler with JSoup in `backend/src/main/java/com/news/crawler/NeteaseCrawler.java`
- [ ] T087 [US3] Implement CrawlerService with async execution in `backend/src/main/java/com/news/service/CrawlerService.java` (depends on T084-T086, T082)
- [ ] T088 [US3] Implement duplicate detection (title matching) in CrawlerService (depends on T087)
- [ ] T089 [US3] Create SchedulerService for timed crawling in `backend/src/main/java/com/news/service/SchedulerService.java` (depends on T087)
- [ ] T090 [US3] Configure @Scheduled task with cron expression in SchedulerService (depends on T089)
- [ ] T091 [US3] Create CrawlerController for manual trigger in `backend/src/main/java/com/news/controller/CrawlerController.java` (depends on T087)
- [ ] T092 [US3] Create ClassificationRuleController in `backend/src/main/java/com/news/controller/ClassificationRuleController.java` (depends on T082)
- [ ] T093 [P] [US3] Create CrawlerManage.vue in `frontend/src/views/admin/CrawlerManage.vue`
- [ ] T094 [P] [US3] Create ClassificationRules.vue in `frontend/src/views/admin/ClassificationRules.vue`
- [ ] T095 [P] [US3] Create crawler API client in `frontend/src/api/crawler.ts`
- [ ] T096 [US3] Implement crawler management UI with task history (depends on T093, T095)
- [ ] T097 [US3] Implement classification rule editor UI (depends on T094, T095)

**Checkpoint**: All three user stories (US1, US2, US3) should now work independently - automatic news collection is functional

---

## Phase 6: User Story 4 - AI 智能新闻摘要生成 (Priority: P4)

**Goal**: 集成智谱AI实现新闻摘要自动生成，支持单条和批量生成，包含重试机制和错误处理

**Independent Test**: 可以通过准备测试新闻内容、触发摘要生成、验证生成的摘要质量和准确性来独立测试

### Tests for User Story 4

- [ ] T098 [P] [US4] Create unit test for ZhipuAiService in `backend/src/test/java/com/news/service/ZhipuAiServiceTest.java`
- [ ] T099 [P] [US4] Create integration test for AI summary generation in `backend/src/test/java/com/news/service/AiSummaryServiceTest.java`
- [ ] T100 [P] [US4] Create E2E test for AI summary feature in `frontend/tests/e2e/ai-summary.spec.ts`

### Implementation for User Story 4

- [ ] T101 [P] [US4] Create Summary entity in `backend/src/main/java/com/news/model/entity/Summary.java`
- [ ] T102 [P] [US4] Create SummaryRepository in `backend/src/main/java/com/news/repository/SummaryRepository.java`
- [ ] T103 [US4] Create ZhipuAiService with RestTemplate in `backend/src/main/java/com/news/service/ZhipuAiService.java`
- [ ] T104 [US4] Add @Retryable annotation for ZhipuAi API calls (depends on T103)
- [ ] T105 [US4] Implement AiSummaryService for single/batch generation in `backend/src/main/java/com/news/service/AiSummaryService.java` (depends on T102, T103)
- [ ] T106 [US4] Create AiSummaryController in `backend/src/main/java/com/news/controller/AiSummaryController.java` (depends on T105)
- [ ] T107 [P] [US4] Create ai API client in `frontend/src/api/ai.ts`
- [ ] T108 [US4] Add "Generate Summary" button to NewsEdit.vue (depends on T062, T107)
- [ ] T109 [US4] Implement batch summary generation UI with progress bar (depends on T061, T107)
- [ ] T110 [US4] Add summary display in news detail view

**Checkpoint**: Four user stories complete - AI summary generation enhances news management

---

## Phase 7: User Story 5 - 新闻浏览和阅读体验 (Priority: P5)

**Goal**: 实现面向普通用户的新闻浏览功能，包括智能排序列表、新闻详情、分类筛选和响应式设计

**Independent Test**: 可以通过预先填充新闻数据、模拟用户浏览和阅读行为、验证界面响应和内容展示来独立测试

### Tests for User Story 5

- [ ] T111 [P] [US5] Create integration test for public news endpoints in `backend/src/test/java/com/news/controller/PublicNewsControllerTest.java`
- [ ] T112 [P] [US5] Create E2E test for user browsing flow in `frontend/tests/e2e/news-browsing.spec.ts`
- [ ] T113 [P] [US5] Create component test for NewsCard in `frontend/tests/unit/NewsCard.spec.ts`

### Implementation for User Story 5

- [ ] T114 [US5] Add public news list endpoint (no auth required) in NewsController (depends on T058)
- [ ] T115 [US5] Add public news detail endpoint with view count increment in NewsController (depends on T058)
- [ ] T116 [US5] Implement smart sorting (time decay + view count) query in NewsRepository (depends on T050)
- [ ] T117 [P] [US5] Create Home.vue page for public in `frontend/src/views/user/Home.vue`
- [ ] T118 [P] [US5] Create NewsDetail.vue page in `frontend/src/views/user/NewsDetail.vue`
- [ ] T119 [P] [US5] Create CategoryNews.vue for category browsing in `frontend/src/views/user/CategoryNews.vue`
- [ ] T120 [P] [US5] Create NewsCard.vue component in `frontend/src/components/news/NewsCard.vue`
- [ ] T121 [P] [US5] Create NewsSummary.vue component in `frontend/src/components/news/NewsSummary.vue`
- [ ] T122 [P] [US5] Create Header.vue with category navigation in `frontend/src/components/layout/Header.vue`
- [ ] T123 [P] [US5] Create Footer.vue in `frontend/src/components/layout/Footer.vue`
- [ ] T124 [US5] Implement news list with smart sorting in Home.vue (depends on T117, T120)
- [ ] T125 [US5] Implement news detail page with AI summary display (depends on T118, T121)
- [ ] T126 [US5] Implement category filtering UI (depends on T119, T120)
- [ ] T127 [US5] Add lazy loading for images using Intersection Observer (depends on T120)
- [ ] T128 [US5] Implement responsive design for mobile/tablet/desktop breakpoints (depends on T117-T119)
- [ ] T129 [US5] Add pagination component in `frontend/src/components/common/Pagination.vue`
- [ ] T130 [US5] Integrate pagination with news list (depends on T124, T129)

**Checkpoint**: All five user stories complete - full system is functional for both admins and users

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T131 [P] Configure Spring Boot Actuator endpoints for health checks
- [ ] T132 [P] Setup Prometheus metrics export in `backend/src/main/resources/application.yml`
- [ ] T133 [P] Configure Logback for structured logging in `backend/src/main/resources/logback-spring.xml`
- [ ] T134 [P] Add API documentation with Swagger/OpenAPI in `backend/pom.xml`
- [ ] T135 [P] Configure CORS for production environment
- [ ] T136 [P] Setup frontend build optimization (code splitting, tree shaking) in `vite.config.ts`
- [ ] T137 [P] Add loading states and skeleton screens across UI
- [ ] T138 [P] Implement error boundary component in `frontend/src/components/common/ErrorBoundary.vue`
- [ ] T139 [P] Add i18n support structure (even if only Chinese initially) in `frontend/src/i18n/`
- [ ] T140 [P] Configure Element Plus theme customization in `frontend/src/styles/element-variables.scss`
- [ ] T141 [P] Add accessibility attributes (ARIA labels) to interactive elements
- [ ] T142 [P] Setup Lighthouse CI for performance monitoring in `.github/workflows/`
- [ ] T143 [P] Create production Docker images (backend Dockerfile, frontend Dockerfile)
- [ ] T144 [P] Update docker-compose.yml for production deployment
- [ ] T145 [P] Write comprehensive README.md with setup instructions
- [ ] T146 [P] Create API documentation in `docs/api.md`
- [ ] T147 [P] Write deployment guide in `docs/deployment.md`
- [ ] T148 Run full E2E test suite validation
- [ ] T149 Perform security audit (dependency scan, OWASP checks)
- [ ] T150 Run performance testing (JMeter or k6) for API load
- [ ] T151 Validate quickstart.md instructions against fresh environment
- [ ] T152 Code cleanup and refactoring pass
- [ ] T153 Final code review and quality gate check

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3 → P4 → P5)
- **Polish (Phase 8)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - No dependencies on other stories, but builds on authentication from US1
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Uses News and Category entities from US2 for classification, but independently testable
- **User Story 4 (P4)**: Can start after Foundational (Phase 2) - Extends News entity from US2, but independently testable
- **User Story 5 (P5)**: Can start after Foundational (Phase 2) - Uses public endpoints but independently testable with mock data

**Recommendation**: Implement sequentially (P1→P2→P3→P4→P5) to maximize learning and reduce integration issues, but parallel development is possible after Phase 2.

### Within Each User Story

- Tests MUST be written FIRST and FAIL before implementation (Test-First approach)
- Models/Entities before Services
- Services before Controllers/APIs
- Backend APIs before Frontend UI
- Core implementation before UI polish
- Story validation before moving to next priority

### Parallel Opportunities

- **Phase 1 (Setup)**: All T001-T009 can run in parallel
- **Phase 2 (Foundational)**: Tasks T015-T025 can run in parallel after T010-T014 complete
- **Within each User Story**:
  - All test tasks marked [P] can run in parallel
  - All entity/model tasks marked [P] can run in parallel
  - All repository tasks marked [P] can run in parallel
  - All DTO tasks marked [P] can run in parallel
  - All frontend component tasks marked [P] can run in parallel
  - Backend and Frontend can proceed in parallel once APIs are designed
- **Phase 8 (Polish)**: All tasks T131-T147 can run in parallel

---

## Parallel Example: User Story 2

```bash
# Test tasks can launch together (T042-T045):
Task T042: Integration test for news CRUD
Task T043: Integration test for category CRUD  
Task T044: Unit test for NewsService
Task T045: E2E test for news management

# Entity creation can happen in parallel (T046-T048):
Task T046: Create Category entity
Task T047: Create News entity
Task T048: Create AuditLog entity

# Repository creation in parallel (T049-T051):
Task T049: Create CategoryRepository
Task T050: Create NewsRepository
Task T051: Create AuditLogRepository

# Frontend pages in parallel (T061-T063):
Task T061: Create NewsList.vue
Task T062: Create NewsEdit.vue
Task T063: Create CategoryManage.vue
```

---

## Implementation Strategy

### MVP First (User Story 1 + User Story 2)

**Minimum Viable Product** delivers core value:
1. Complete Phase 1: Setup (T001-T009)
2. Complete Phase 2: Foundational (T010-T025) - **CRITICAL BLOCKER**
3. Complete Phase 3: User Story 1 (T026-T041) - Authentication
4. Complete Phase 4: User Story 2 (T042-T072) - News Management
5. **STOP and VALIDATE**: Test US1+US2 independently
6. Deploy/demo if ready - Admins can log in and manage news

**MVP Delivers**:
- ✅ Secure admin access
- ✅ Full news CRUD
- ✅ Category management
- ✅ Search and filtering
- ✅ Audit logging

### Incremental Delivery

1. **Foundation** (Phase 1 + 2) → Infrastructure ready
2. **MVP** (Phase 3 + 4) → Core admin features → Deploy/Demo 🎯
3. **+ Automation** (Phase 5) → News crawling → Deploy/Demo
4. **+ AI Enhancement** (Phase 6) → Smart summaries → Deploy/Demo
5. **+ User Experience** (Phase 7) → Public browsing → Deploy/Demo
6. **Production Ready** (Phase 8) → Polish & monitoring → Final Release

Each increment adds value without breaking previous features.

### Parallel Team Strategy

With 3+ developers:

1. **Week 1**: Team completes Setup + Foundational together (Phase 1-2)
2. **Week 2-3**: Once Foundational is done:
   - **Developer A**: User Story 1 (Authentication) - blocks admin features
   - **Developer B**: Prepare User Story 2 tests and frontend components
   - **Developer C**: Configure infrastructure (CI/CD, monitoring)
3. **Week 4-5**: After US1 complete:
   - **Developer A**: User Story 3 (Crawler)
   - **Developer B**: User Story 2 (News Management)
   - **Developer C**: User Story 5 (User Interface)
4. **Week 6**: 
   - **Developer A**: User Story 4 (AI Summary)
   - **Developer B/C**: Polish & testing

---

## Task Summary

**Total Tasks**: 153

**Task Count by Phase**:
- Phase 1 (Setup): 9 tasks
- Phase 2 (Foundational): 16 tasks (CRITICAL BLOCKER)
- Phase 3 (US1 - Auth): 16 tasks
- Phase 4 (US2 - News Management): 31 tasks
- Phase 5 (US3 - Crawler): 25 tasks
- Phase 6 (US4 - AI Summary): 13 tasks
- Phase 7 (US5 - User Browsing): 20 tasks
- Phase 8 (Polish): 23 tasks

**Parallel Opportunities**: 
- ~60% of tasks can run in parallel with proper team coordination
- Each user story is independently testable

**Estimated Timeline** (single developer, full-time):
- Phase 1-2: 1 week (foundation)
- Phase 3 (US1): 3-4 days (MVP critical)
- Phase 4 (US2): 1 week (MVP critical)
- Phase 5 (US3): 5-6 days
- Phase 6 (US4): 3-4 days
- Phase 7 (US5): 5-6 days
- Phase 8: 3-4 days

**Total**: ~6-7 weeks for full system

**MVP** (US1+US2 only): ~2.5 weeks

---

## Notes

- **[P] tasks** = different files, no dependencies, can execute in parallel
- **[Story] label** maps task to specific user story for traceability
- **Each user story** is independently completable and testable
- **Test-First**: Write tests, verify they FAIL, then implement
- **Commit frequently**: After each task or logical group
- **Stop at checkpoints**: Validate story independently before proceeding
- **Avoid**: vague tasks, same file conflicts, cross-story dependencies that break independence

---

## Success Criteria

Each checkpoint should verify:
- ✅ All tests pass (unit, integration, E2E)
- ✅ Code coverage ≥ 80%
- ✅ No linter errors
- ✅ Performance benchmarks met (API P95 < 200ms, LCP < 2.5s)
- ✅ User story acceptance scenarios validated
- ✅ Documentation updated (API docs, README)

Ready to start? Begin with T001! 🚀

