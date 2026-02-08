# SkillSync-AI 🚀

**SkillSync-AI** is an intelligent recruitment platform that leverages Artificial Intelligence to streamline the hiring process. It connects recruiters with the best-fit candidates by analyzing resumes, matching skills, and automating interview scheduling.

## 🌟 Key Features

### 🤖 AI-Powered Matching
- **Resume Parsing:** Automatically extracts skills, experience, and education from PDF resumes using AI.
- **Smart Matching:** Uses vector embeddings (pgvector) to semantically match candidates to job descriptions.
- **Match Score:** Provides a compatibility score and explanation for every candidate-job pair.

### 📩 Invite-to-Apply System
- **Proactive Hiring:** Recruiters can invite top-matched candidates to apply.
- **Secure Flow:** Candidates receive unique, time-limited invitation links via email.
- **One-Click Acceptance:** Accepting an invite automatically creates a job application.

### 📅 Automated Interview Management
- **Smart Scheduling:** Streamlined interview scheduling for recruiters.
- **Calendar Integration:** Automatically generates and emails `.ics` calendar invites for Google Calendar/Outlook.
- **Lifecycle Management:** Handle rescheduling and cancellations with automated email notifications to both parties.

### 👥 User Roles
- **Recruiters:** Post jobs, view matches, invite candidates, schedule interviews.
- **Candidates:** Create profiles, upload resumes, view matches, apply to jobs, manage invitations.

---

## 🛠️ Tech Stack

- **Backend:** Java 17, Spring Boot 3.x
- **Database:** PostgreSQL (with `pgvector` extension)
- **AI & ML:** Spring AI, Google Vertex AI (Gemini Models)
- **Authentication:** JWT (JSON Web Tokens) with Spring Security
- **Email:** JavaMailSender (SMTP)
- **Build Tool:** Maven

---

## 🚀 Getting Started

### Prerequisites
1.  **Java 17+** installed.
2.  **PostgreSQL** installed with `pgvector` extension enabled.
3.  **Google Cloud Project** with Vertex AI API enabled.
4.  **SMTP Server** credentials (e.g., Gmail App Password).

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/yourusername/SkillSync-AI.git
    cd SkillSync-AI
    ```

2.  **Database Setup:**
    - Create a database named `vectordb`.
    - Enable the vector extension:
      ```sql
      CREATE EXTENSION IF NOT EXISTS vector;
      ```

3.  **Configuration:**
    - Update `src/main/resources/application.yml` with your credentials or set them as environment variables:
      - `SPRING_DATASOURCE_USERNAME` / `PASSWORD`
      - `GOOGLE_CLOUD_PROJECT_ID`
      - `MAIL_USERNAME` / `MAIL_PASSWORD`

4.  **Run the Application:**
    ```bash
    mvn spring-boot:run
    ```

---

## 🔌 API Endpoints Overview

| Module | Method | Endpoint | Description |
| :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/auth/login` | User login (returns JWT) |
| **Jobs** | `POST` | `/jobs` | Post a new job (Recruiter) |
| | `GET` | `/jobs/{id}/matches` | Get matched candidates for a job |
| **Invites** | `POST` | `/jobs/{id}/invite` | Invite a candidate to apply |
| | `POST` | `/invitations/{token}/accept` | Accept job invitation |
| **Interviews** | `POST` | `/interviews/schedule` | Schedule an interview |
| | `POST` | `/interviews/{id}/reschedule` | Reschedule an interview |

---

## 🤝 Contribution

Contributions are welcome! Please fork the repository and submit a Pull Request.

## 📄 License

This project is licensed under the MIT License.
