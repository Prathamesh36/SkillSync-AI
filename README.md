# 🚀 SkillSync AI

**An AI-powered hiring platform that uses semantic search and intelligent matching to connect the right candidates with the right jobs — automatically.**

---

## 📌 Problem Statement

Traditional hiring platforms rely on keyword-based filtering, which fails to capture the nuance of a candidate's true capabilities. Recruiters manually sift through hundreds of resumes, candidates apply blindly to jobs that don't match their skills, and interviews are scheduled through tedious back-and-forth emails. The process is slow, biased toward keyword-stuffed resumes, and fundamentally broken.

## 💡 Solution Overview

SkillSync AI reimagines the hiring pipeline with AI at every stage:

- **Resume Parsing** — AI extracts structured data (skills, experience, education) from uploaded resumes using natural language understanding, not regex.
- **Semantic Matching** — Vector embeddings and cosine similarity surface truly relevant candidates, even when exact keywords don't match.
- **AI Mock Interviews** — Candidates practice with an adaptive AI interviewer that generates contextual questions based on their resume and evaluates responses in real-time.
- **Smart Recommendations** — Candidates receive AI-powered job recommendations with natural language explanations of why each job fits their profile.
- **Interview Scheduling** — Recruiters schedule, reschedule, or cancel interviews with automatic email notifications and `.ics` calendar invites.

---
## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language runtime |
| Spring Boot | 3.5.10 | Application framework |
| Spring AI | 1.1.2 | LLM integration (chat, embeddings, structured output) |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | — | ORM / data access |
| Spring Mail | — | Email notifications (SMTP) |
| Spring Retry | — | Retry logic for AI calls |
| PostgreSQL | 16 | Relational database |
| pgvector | — | Vector similarity search |
| Apache Tika | — | Document text extraction |
| jjwt | 0.12.5 | JWT token generation & validation |
| ModelMapper | 3.2.0 | DTO ↔ Entity mapping |
| Lombok | — | Boilerplate reduction |
| Maven | — | Build tool |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| React | 19.2 | UI framework |
| Vite | 7.2 | Build tool & dev server |
| React Router | 7.13 | Client-side routing |
| Axios | 1.13 | HTTP client |
| Framer Motion | 12.33 | Animations & transitions |
| React Hot Toast | 2.6 | Toast notifications |

### Infrastructure
| Technology | Purpose |
|---|---|
| Docker + Docker Compose | Containerization |
| Kubernetes | Orchestration (optional) |
| Nginx | Frontend static serving |
| PostgreSQL + pgvector | Database + vector store |
| Ollama | Local LLM inference (fallback) |
| OpenRouter | Cloud AI model routing |

---

## ✨ Key Features

### For Recruiters
- **Post & manage jobs** with detailed descriptions, required skills, experience levels, and employment types
- **AI candidate matching** — find the best-fit candidates for any job using vector-based semantic search
- **Invite-to-Apply** — proactively invite matched candidates with personalized messages and email notifications
- **Application pipeline management** — review, shortlist, and track candidate applications
- **Interview scheduling** — schedule interviews (online/in-person) with automatic calendar invites (`.ics`)
- **Reschedule & cancel** interviews with real-time email notifications to both parties
- **Dashboard analytics** — view job stats, application counts, and recruiter profile completion

### For Candidates
- **Upload & parse resumes** — AI extracts structured profile data automatically
- **Browse & filter jobs** — search by skills, location, experience, employment type
- **AI job recommendations** — personalized job suggestions with AI-generated explanations for why each job matches
- **One-click apply** — apply to jobs with your parsed resume
- **AI mock interviews** — practice with an AI interviewer that adapts questions to your skills and evaluates your answers (resume-based or topic-based)
- **Interview history & transcripts** — review past mock interview sessions with scores and feedback
- **View scheduled interviews** — track real interview schedules set by recruiters
- **Job invitations** — receive and accept/decline recruiter invitations

---

## 🗂️ Entity Relationship (ER) Diagram

```mermaid
%% Paste your full ER diagram code below this line

erDiagram
    USER ||--o{ CANDIDATE : has
    USER ||--o{ RECRUITER : has
    USER ||--o{ RESUME : owns
    USER ||--o{ APPLICATION : submits
    USER ||--o{ JOB_INVITATION : receives
    USER ||--o{ INTERVIEW : participates
    USER ||--o{ MATCH_RESULT : generates
    
    CANDIDATE ||--o{ RESUME : owns
    CANDIDATE ||--o{ APPLICATION : submits
    CANDIDATE ||--o{ JOB_INVITATION : receives
    CANDIDATE ||--o{ INTERVIEW : participates
    CANDIDATE ||--o{ MATCH_RESULT : generates
    
    RECRUITER ||--o{ JOB : posts
    RECRUITER ||--o{ INTERVIEW : schedules
    RECRUITER ||--o{ JOB_INVITATION : sends
    
    JOB ||--o{ APPLICATION : receives
    JOB ||--o{ JOB_INVITATION : sends
    JOB ||--o{ INTERVIEW : has
    JOB ||--o{ MATCH_RESULT : generates
    
    RESUME ||--o{ MATCH_RESULT : used_in
    
    USER {
        Long id PK
        String email UK
        String password
        String role
        String fullName
        String phoneNumber
        String bio
        Boolean isActive
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }
    
    CANDIDATE {
        Long id PK
        Long userId UK
        String headline
        String location
        Integer experienceYears
        List<String> skills
        String resumeUrl
        String resumeText
        String resumeVector
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }
    
    RECRUITER {
        Long id PK
        Long userId UK
        String companyName
        String companyLogoUrl
        String department
        String designation
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }
    
    JOB {
        Long id PK
        Long recruiterId
        String title
        String description
        String location
        BigDecimal salaryMin
        BigDecimal salaryMax
        Integer requiredExperienceYears
        List<String> skillsRequired
        String jobType
        String employmentType
        Boolean isActive
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }
    
    RESUME {
        Long id PK
        Long userId
        String fileName
        String fileUrl
        String parsedContent
        List<String> extractedSkills
        String embeddingVector
        LocalDateTime uploadedAt
    }
    
    APPLICATION {
        Long id PK
        Long jobId
        Long candidateId
        String status
        String coverLetter
        LocalDateTime appliedAt
        LocalDateTime updatedAt
    }
    
    JOB_INVITATION {
        Long id PK
        Long jobId
        Long candidateId
        String invitationToken
        String status
        LocalDateTime sentAt
        LocalDateTime expiresAt
        LocalDateTime acceptedAt
    }
    
    INTERVIEW {
        Long id PK
        Long jobId
        Long candidateId
        Long recruiterId
        LocalDateTime interviewDateTime
        String interviewType
        String status
        String location
        String meetingLink
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }
    
    MATCH_RESULT {
        Long id PK
        Long jobId
        Long candidateId
        Double semanticScore
        Double skillScore
        Double experienceScore
        Double locationScore
        Double finalScore
        String explanation
        LocalDateTime createdAt
    }



```
---

## 🏗️ System Architecture

### Backend — Layered Design

```
┌───────────────────────────────────────────────┐
│                 Controller Layer               │
│  AuthController · JobController · UserController│
│  ResumeController · InterviewController · ...  │
├───────────────────────────────────────────────┤
│                 Service Layer                  │
│  (Interface + Impl for all 16 services)        │
│  AIService · JobMatchingService · VectorSearch │
│  InterviewAiService · NotificationService · ...│
├───────────────────────────────────────────────┤
│               Repository Layer                 │
│  Spring Data JPA Repositories                  │
├───────────────────────────────────────────────┤
│                Entity Layer                    │
│  User · Candidate · Recruiter · Job · Resume   │
│  Application · MatchResult · InterviewSchedule │
│  InterviewSession · InterviewTranscript · ...  │
├───────────────────────────────────────────────┤
│              Infrastructure                    │
│  PostgreSQL + pgvector · Spring Security/JWT   │
│  Spring Mail · Spring AI · Apache Tika         │
└───────────────────────────────────────────────┘
```

### Frontend–Backend Interaction
- React frontend communicates via **REST API** over HTTP (Axios)
- JWT tokens stored in `localStorage`, attached via Axios request interceptor
- Role-based routing: separate dashboard layouts for **Candidate** and **Recruiter**
- API modules: `authAPI`, `userAPI`, `jobsAPI`, `applicationsAPI`, `invitationsAPI`, `mockInterviewAPI`

### Storage Architecture
- **PostgreSQL** (pgvector/pg16) — relational data + vector embeddings in a single database (`vectordb`)
- **pgvector extension** — initialized via `init-db.sql` (`CREATE EXTENSION IF NOT EXISTS vector`)
- **Resume files** — stored on the server filesystem (`uploads/` directory)
- **Vector Store** — `PgVectorStore` managed by Spring AI for automatic embedding storage and similarity search

---

## 🚢 Deployment Architecture

### Docker Compose (Full Stack)

| Service      | Image / Build                | Port   |
|-------------|------------------------------|--------|
| **postgres** | `pgvector/pgvector:pg16`     | `5454` |
| **backend**  | Multi-stage Maven + Corretto 21 | `9090` |
| **ollama**   | `ollama/ollama:latest`       | `11434`|
| **frontend** | Multi-stage Node 20 + Nginx  | `5173` |

- Backend waits for PostgreSQL health check before starting
- Persistent volumes for PostgreSQL data and Ollama models
- Environment variables injected from `.env` file

### Kubernetes
K8s manifests provided for:
- `postgres.yaml` — PostgreSQL StatefulSet with PVC
- `backend.yaml` — Spring Boot Deployment + Service
- `frontend.yaml` — Nginx-served React app + Service

### Cloud Deployment
- **Frontend**: Deployable to **Vercel** (static build output from Vite)
- **Backend**: Containerized with multi-stage Dockerfile (Amazon Corretto 21 Alpine)

---

## 🧠 AI Architecture

### Resume Parsing
Resumes (PDF, DOCX) are processed through **Apache Tika** for text extraction, then sent to an LLM to extract structured data:
- Full name, email, skills list, years of experience, education summary, professional summary
- Output is mapped to a typed `ParsedResumeDTO` using Spring AI's structured output (entity extraction)

### Embeddings & Semantic Search
- Resume content is embedded using **OpenAI embedding models** and stored in **PostgreSQL with pgvector**
- Job descriptions are also embedded and stored in the vector store with `docType` metadata (`RESUME` or `JOB`)
- **Similarity search** uses `SearchRequest` with configurable `topK`, `similarityThreshold`, and `FilterExpression` to find relevant matches
- This enables true semantic matching — "React developer" matches candidates with "frontend engineering" experience, even without exact keyword overlap

### Match Score Calculation
A **hybrid scoring model** combines:
- **Skill overlap** (75% weight) — fuzzy substring matching between job requirements and candidate skills
- **Experience alignment** (25% weight) — proportional scoring based on required vs. actual experience
- Base score of 30% ensures minimum visibility; total score is capped at 100%

### AI-Powered Explanations
- Candidates receive **natural language explanations** of why a job was recommended, generated by the LLM using their profile and the job description
- Fallback from OpenAI to Ollama is built in

### AI Mock Interviews
- **Resume-based** — AI generates questions tailored to the candidate's skills and experience level
- **Topic-based** — candidate selects topics and difficulty; AI generates contextual questions
- Each answer is evaluated in real-time with a JSON-structured response: `score (0–10)`, `strengths`, `weaknesses`
- After 5 questions, AI generates a final feedback summary

### AI Fallback Strategy
Every AI call follows a **primary → fallback** pattern:
1. **Primary**: OpenAI-compatible API (configured via OpenRouter for flexible model routing)
2. **Fallback**: Local **Ollama** instance (default model: `gemma3:latest`)
3. **Retry**: Spring Retry with `@EnableRetry` handles transient failures (rate limits, timeouts)

---

## 📡 API Documentation

> Base URL: `http://localhost:9090/api`
> All endpoints except those marked 🌐 (Public) require a valid JWT token in the `Authorization: Bearer <token>` header.

---

### 🔐 Authentication

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/auth/login` | 🌐 Public | Authenticate with email and password; returns JWT token and user details |

---

### 👤 Users

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/users` | 🌐 Public | Register a new user account (CANDIDATE or RECRUITER role) |
| `GET` | `/api/users/me` | Authenticated | Get the currently logged-in user's profile information |
| `GET` | `/api/users/{id}` | Authenticated | Fetch a specific user's details by their ID |
| `GET` | `/api/users` | Authenticated | List all registered users in the system |
| `PUT` | `/api/users/{id}` | Authenticated | Update a user's profile (name, bio, LinkedIn URL, etc.) |
| `DELETE` | `/api/users/{id}` | Authenticated | Delete a user account permanently |

---

### 📄 Resumes

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/resumes/upload` | CANDIDATE | Upload a resume file (PDF/DOCX); AI parses it, extracts skills/experience, stores embedding in vector DB |
| `DELETE` | `/api/resumes/me` | CANDIDATE | Delete the current candidate's resume (blocked if resume is linked to active applications) |
| `GET` | `/api/resumes/download/{resumeId}` | Authenticated | Download a resume file by its ID as an attachment |

---

### 💼 Jobs

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/jobs` | RECRUITER | Create a new job posting (requires 100% profile completion) |
| `GET` | `/api/jobs` | Authenticated | List all active job postings |
| `GET` | `/api/jobs/{id}` | Authenticated | Get full details of a specific job |
| `GET` | `/api/jobs/my` | RECRUITER | List all jobs posted by the authenticated recruiter |
| `PUT` | `/api/jobs/{id}` | RECRUITER | Update an existing job posting (owner only) |
| `DELETE` | `/api/jobs/{id}` | RECRUITER | Delete a job posting (owner only) |
| `GET` | `/api/jobs/search` | 🌐 Public | Search jobs by keyword query (title, description, skills) |
| `GET` | `/api/jobs/filter` | Authenticated | Filter jobs by type, employment type, location, salary range, and skill |
| `PATCH` | `/api/jobs/{id}/status` | RECRUITER | Toggle a job's active/inactive status (owner only) |

---

### 📋 Applications

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/jobs/{jobId}/apply` | CANDIDATE | Apply for a job with an uploaded resume |
| `GET` | `/api/candidates/me/applications` | CANDIDATE | List all job applications submitted by the current candidate |
| `GET` | `/api/candidates/me/interviews` | CANDIDATE | List all scheduled interviews for the current candidate |
| `GET` | `/api/recruiter/jobs/{jobId}/applications` | RECRUITER | View all applications for a specific job, optionally filtered by status |
| `GET` | `/api/recruiter/applications` | RECRUITER | View all applications across all jobs posted by the recruiter |
| `GET` | `/api/recruiter/stats` | RECRUITER | Get dashboard statistics (total jobs, applications, shortlisted, interviews) |
| `PATCH` | `/api/applications/{applicationId}/status` | RECRUITER | Update an application's status (e.g., REVIEWED, REJECTED, OFFERED) |
| `PATCH` | `/api/applications/{applicationId}/shortlist` | RECRUITER | Shortlist a candidate's application for further review |

---

### 📅 Interview Scheduling

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/applications/{applicationId}/schedule-interview` | RECRUITER | Schedule an interview for a shortlisted candidate (sends email + .ics calendar invite) |
| `PATCH` | `/api/interviews/{interviewId}/reschedule` | RECRUITER | Reschedule an existing interview to a new date/time (sends updated notification) |
| `PATCH` | `/api/interviews/{interviewId}/cancel` | RECRUITER | Cancel a scheduled interview with a reason (reverts application to SHORTLISTED) |
| `GET` | `/api/recruiter/jobs/{jobId}/interviews` | RECRUITER | List all scheduled interviews for a specific job |
| `GET` | `/api/recruiter/interviews` | RECRUITER | List all scheduled interviews across all recruiter's jobs |

---

### 🤖 AI Mock Interviews

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/interviews/mock/start` | CANDIDATE | Start a resume-based mock interview; AI generates the first question from candidate profile |
| `POST` | `/api/interviews/mock/topic/start` | CANDIDATE | Start a topic-based mock interview with custom topics and difficulty level |
| `POST` | `/api/interviews/mock/{sessionId}/answer` | CANDIDATE | Submit an answer to the current question; AI evaluates with score, strengths, and weaknesses |
| `POST` | `/api/interviews/mock/{sessionId}/end` | CANDIDATE | End the interview session early; AI generates final feedback summary |
| `GET` | `/api/interviews/mock/{sessionId}/transcript` | CANDIDATE | Retrieve the full Q&A transcript with evaluations for a completed session |
| `GET` | `/api/interviews/mock/history` | CANDIDATE | List all past completed mock interview sessions with scores |

---

### 🎯 AI Job Matching (Recruiter)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `GET` | `/api/jobs/{jobId}/matches` | RECRUITER | Find top matching candidates for a job using vector similarity search + hybrid scoring |

---

### 💡 AI Job Recommendations (Candidate)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `GET` | `/api/candidates/me/recommended-jobs` | CANDIDATE | Get AI-powered job recommendations based on resume embeddings; filterable by score threshold and location |
| `GET` | `/api/candidates/me/recommended-jobs/{jobId}/explanation` | CANDIDATE | Get an AI-generated natural language explanation of why a specific job is recommended |

---

### 📩 Job Invitations

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/jobs/{jobId}/invite` | RECRUITER | Invite a matched candidate to apply for a job (sends email notification with invite link) |
| `GET` | `/api/candidates/me/invitations` | CANDIDATE | List all job invitations received by the current candidate |
| `POST` | `/api/invitations/{token}/accept` | CANDIDATE | Accept a job invitation using the secure token; auto-creates a job application |
| `POST` | `/api/invitations/{token}/decline` | CANDIDATE | Decline a job invitation using the secure token |

---

## 🖥️ Local Setup Instructions

### Prerequisites
- Java 21 (JDK)
- Node.js 20+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16 with pgvector extension **or** use Docker

### 1. Clone the Repository
```bash
git clone https://github.com/Prathamesh36/SkillSync-AI.git
cd SkillSync-AI
```

### 2. Start Infrastructure (Database + Ollama)
```bash
cd SkillSync-AI
docker compose up -d postgres ollama
```

### 3. Configure Environment Variables
```bash
cp .env.example .env
# Edit .env with your actual values
```

### 4. Run the Backend
```bash
cd SkillSync-AI
mvn spring-boot:run
```
Backend starts on `http://localhost:9090`

### 5. Run the Frontend
```bash
cd skillsync-ai-frontend
npm install
npm run dev
```
Frontend starts on `http://localhost:5173`

---

## 🐳 Docker Setup (Full Stack)

```bash
cd SkillSync-AI

# Create .env file with required variables
cp .env.example .env

# Build and run all services
docker compose up --build -d
```

This starts:
- **PostgreSQL** (pgvector) on port `5454`
- **Ollama** on port `11434`
- **Backend** on port `9090`
- **Frontend** on port `5173`

---

## 🔐 Environment Variables

| Variable | Description | Required |
|---|---|---|
| `OPENAI_API_KEY` | API key for OpenRouter / OpenAI-compatible endpoint | ✅ |
| `MAIL_USERNAME` | Gmail address for sending notifications | ✅ |
| `MAIL_PASSWORD` | Gmail app-specific password | ✅ |
| `SPRING_AI_OLLAMA_BASE_URL` | Ollama base URL (default: `http://localhost:11434`) | Optional |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL (default: `jdbc:postgresql://localhost:5454/vectordb`) | Optional |
| `SPRING_DATASOURCE_USERNAME` | Database username (default: `root`) | Optional |
| `SPRING_DATASOURCE_PASSWORD` | Database password (default: `root`) | Optional |

---

## 🔒 Security Implementation

### JWT Authentication
- Stateless authentication using **JWT tokens** (jjwt 0.12.5)
- `JwtTokenProvider` handles token generation and validation
- `JwtAuthenticationFilter` intercepts every request and sets the security context
- Tokens transmitted via `Authorization: Bearer <token>` header

### Role-Based Access Control
- Two roles: **CANDIDATE** and **RECRUITER**
- `@EnableMethodSecurity` enables method-level authorization
- Public endpoints: `/api/auth/**`, `/api/jobs/search`, `POST /api/users`
- All other endpoints require authentication
- `CustomUserDetailsService` loads users from the database for authentication

### CORS Configuration
- Configured allowed origins for local development and Kubernetes NodePort access
- Supports all standard HTTP methods with credential forwarding
- Preflight responses cached for 1 hour

### Secure Password Storage
- Passwords hashed with **BCrypt** (`BCryptPasswordEncoder`)

---

## 🛡️ AI Resilience Strategy

```
┌──────────────┐     Fail     ┌──────────────┐     Fail     ┌──────────────┐
│   OpenRouter  │ ──────────► │    Ollama     │ ──────────► │  Graceful     │
│  (Cloud AI)   │             │  (Local LLM)  │             │  Degradation  │
└──────────────┘             └──────────────┘             └──────────────┘
       │                            │
       └──── Spring Retry ──────────┘
             (Rate limit / Timeout handling)
```

- **Primary**: OpenAI-compatible API via OpenRouter (`openrouter/free` model)
- **Fallback**: Local Ollama instance with `gemma3:latest`
- **Retry**: Spring Retry with `@EnableRetry` for transient failures
- **Parsing resilience**: If AI extraction fails, both providers are attempted before throwing
- **Evaluation resilience**: JSON parsing of AI responses includes fallback defaults if malformed

---

## 🔮 Future Enhancements

- [ ] Real-time chat between recruiters and candidates (WebSocket)
- [ ] Video interview integration (WebRTC)
- [ ] Advanced analytics dashboard with hiring funnel metrics
- [ ] Resume builder with AI suggestions
- [ ] Multi-language support for global hiring
- [ ] OAuth 2.0 social login (Google, LinkedIn)
- [ ] Swagger/OpenAPI documentation endpoint
- [ ] S3/MinIO integration for cloud resume storage
- [ ] Rate limiting and API throttling
- [ ] Candidate skill assessment modules

---

## 🎬 Demo Video

> 📹 [Watch the demo video](#) *(link to be added)*

---

## 👨‍💻 Author & Credits

**Prathamesh Patil**

Built as part of the **CodingShuttle Hackathon** — AI-Powered Online Hiring Platform.

| | |
|---|---|
| **GitHub** | [Prathamesh36](https://github.com/Prathamesh36) |
| **Project** | [SkillSync-AI](https://github.com/Prathamesh36/SkillSync-AI) |

---

<p align="center">
  Built with ☕ Spring Boot, ⚛️ React, and 🤖 Spring AI
</p>
