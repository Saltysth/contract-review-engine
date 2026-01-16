# 合同审查引擎 (Contract Review Engine)

基于DDD架构和管道模式的合同审查引擎服务，提供合同解析、条款提取、风险评估等功能。

## 项目架构

### 技术栈
- **Java 17** + **Spring Boot 3.5.0**
- **PostgreSQL** + **Flyway** (数据库迁移)
- **Redis** (缓存)
- **RabbitMQ** (消息队列)
- **Lombok** (代码生成)
- **OpenAPI/Swagger** (API文档)

### 架构设计
采用领域驱动设计(DDD)和管道模式：

```
├── domain/                 # 领域层
│   ├── model/             # 聚合根和实体
│   ├── valueobject/       # 值对象
│   ├── enums/            # 枚举
│   └── repository/       # 仓储接口
├── application/           # 应用层
│   └── service/          # 应用服务
├── infrastructure/        # 基础设施层
│   ├── pipeline/         # 审查管道
│   └── external/         # 外部服务客户端
├── interfaces/           # 接口层
│   └── rest/            # REST控制器和DTO
└── config/              # 配置类
```

## 核心功能

### 1. 审查管道 (Review Pipeline)
四阶段处理流程：
- **解析阶段** (Parsing): 文档解析和文本提取
- **提取阶段** (Extraction): 条款识别和分类
- **分析阶段** (Analysis): 合规性和完整性分析
- **风险评估** (Risk Assessment): 风险因子评估和等级确定

### 2. 领域模型
- **Task**: 任务聚合根，管理任务生命周期
- **ContractTask**: 合同审查任务，继承Task
- **ReviewResult**: 审查结果聚合，包含风险评估和提取条款
- **TaskConfiguration**: 任务配置值对象
- **RiskAssessment**: 风险评估值对象

### 3. REST API
提供完整的RESTful API：
- 任务管理 (`/api/v1/tasks`)
- 合同审查 (`/api/v1/contract-review`)
- 统计信息和监控

## 数据库设计

### 核心表结构
```sql
-- 任务表
tasks (id, task_type, status, configuration, retry_count, ...)

-- 合同任务表
contract_tasks (id, contract_id, file_path, file_hash, review_type, ...)

-- 审查结果表
review_results (task_id, current_stage, risk_assessment, analysis_result, ...)

-- 提取条款表
extracted_clauses (id, task_id, clause_type, content, confidence, ...)

-- 操作日志表
operation_logs (id, task_id, operation_type, details, ...)
```

## 快速开始

### 1. 环境要求
- Java 17+
- PostgreSQL 12+
- Redis 6+
- RabbitMQ 3.8+

### 2. 数据库初始化
```bash
# 创建数据库
createdb contract_review_engine

# 设置环境变量
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

### 3. 启动应用
```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 4. API文档
启动后访问: http://localhost:8080/contract-review-engine/swagger-ui.html

## 配置说明

### 应用配置 (application.yml)
```yaml
app:
  review:
    default-timeout-minutes: 30
    max-retry-attempts: 3
    supported-file-types: [pdf, doc, docx, txt]
    external-services:
      llm-api:
        base-url: ${LLM_API_BASE_URL}
      file-storage:
        base-url: ${FILE_STORAGE_BASE_URL}
```

### 环境变量
- `DB_USERNAME`: 数据库用户名
- `DB_PASSWORD`: 数据库密码
- `REDIS_HOST`: Redis主机地址
- `RABBITMQ_HOST`: RabbitMQ主机地址
- `LLM_API_BASE_URL`: LLM服务API地址
- `FILE_STORAGE_BASE_URL`: 文件存储服务地址

## API使用示例

### 创建审查任务
```bash
curl -X POST http://localhost:8080/contract-review-engine/api/v1/contract-review/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "contractId": "CONTRACT-2024-001",
    "filePath": "/contracts/2024/contract-001.pdf",
    "fileHash": "sha256:abc123...",
    "reviewType": "FULL_REVIEW",
    "priority": 5,
    "timeoutMinutes": 30
  }'
```

### 启动审查
```bash
curl -X POST http://localhost:8080/contract-review-engine/api/v1/contract-review/tasks/{taskId}/start
```

### 获取审查结果
```bash
curl http://localhost:8080/contract-review-engine/api/v1/contract-review/tasks/{taskId}/result
```

## 监控和运维

### 健康检查
- 应用健康: `/actuator/health`
- 指标监控: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`

### 日志配置
- 日志文件: `logs/contract-review-engine.log`
- 日志级别: 开发环境DEBUG，生产环境WARN
- 日志轮转: 100MB/文件，保留30天

## 开发指南

### 代码规范
- 使用Lombok减少样板代码
- 遵循DDD设计原则
- 所有实体使用JPA注解
- 异常处理和日志记录

### 扩展指南
1. **添加新的管道阶段**: 实现`PipelineStage`接口
2. **扩展领域模型**: 在domain包下添加新的聚合根或值对象
3. **集成外部服务**: 在infrastructure/external包下添加客户端
4. **添加新的API**: 在interfaces/rest包下创建控制器

## 待实现功能 (TODO)

### 外部服务集成
- [ ] LLM API客户端实现
- [ ] 文件存储服务集成
- [ ] 合同管理系统集成

### 高级功能
- [ ] 异步任务处理
- [ ] 断路器模式
- [ ] 分布式锁
- [ ] 审查结果缓存

### 监控和告警
- [ ] 自定义指标
- [ ] 告警规则
- [ ] 性能监控

## 许可证
MIT License

## 联系方式
开发者: 1432488520@qq.com