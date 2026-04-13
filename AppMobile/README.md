# 🔐 Sistema de Controle de Acesso com RFID

Projeto de um sistema de catraca inteligente utilizando RFID, desenvolvido com **Spring Boot** e **Firebase (Firestore)**.

## 🚀 Funcionalidades

* 📡 Validação de cartões RFID
* 👤 Vinculação de cartão a aluno
* 🔓 Liberação ou bloqueio de acesso
* 📝 Registro de logs de entrada em tempo real

## 🛠️ Tecnologias

* Java + Spring Boot
* Firebase Firestore
* REST API
* Postman (testes)

## 🔗 Endpoints

### Validar RFID

`POST /rfid/validar`

```json
{
  "uid": "ID_DO_CARTAO"
}
```

### Vincular RFID

`POST /rfid/vincular`

```json
{
  "uid": "ID_DO_CARTAO",
  "alunoId": "ID_DO_ALUNO"
}
```

## 🧠 Conceitos aplicados

* Integração com serviços externos (Firebase)
* Modelagem NoSQL
* API REST
* Controle de acesso em tempo real

## 🎯 Objetivo

Simular um sistema real de controle de entrada (como escolas, academias ou empresas), integrando hardware (RFID/ESP32) com backend moderno.

---

💡 Futuras melhorias:

* Integração com ESP32
* Dashboard web para administração
* Autenticação de usuários
