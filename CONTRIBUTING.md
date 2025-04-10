# Contributing to rKitPvP

Thank you for considering contributing to this project! Whether it's reporting bugs, suggesting features, or submitting pull requests — **every bit helps**. This guide will walk you through how to contribute effectively.

---

## 🛠️ Getting Started

### 1. Fork the Repository

Click the **Fork** button on the top right of the GitHub page to create your own copy of the repository.

### 2. Clone Your Fork

```bash
git clone https://github.com/Rhydium/rKitPvP.git
cd rKitPvP
```

### 3. Create a Branch

Always create a new branch before making changes:

```bash
git checkout -b my-feature
```

---

## 🧑‍💻 Development Guidelines

- Stick to **Java 21**.
- Use **clear and descriptive names** for classes, variables, and methods.
- Keep the code clean and modular — break large functions into smaller ones when possible.
- Use in-line comments only when necessary. Let your code speak for itself.
- Avoid hardcoding strings and values; prefer configs where applicable.

---

## 📁 Project Structure

Typical plugin structure:

```
/src
  /main
    /java
      /me
        /rhydium
          /rKitPvP
              rKitPvP.java
              commands/
              listeners/
              managers/
              utils/
  /resources
      plugin.yml
      config.yml
```

---

## 🐛 Reporting Issues

When reporting a bug, please include the following:

- Minecraft version and server type (e.g. Paper 1.21.4)
- Plugin version
- Exact error messages (if any)
- Steps to reproduce the issue

---

## ✅ Submitting a Pull Request

1. Ensure your changes are tested and working.
2. Run `./gradlew build` or your IDE’s build tool to make sure there are no errors.
3. Open a pull request against the `main` branch.
4. In your PR description, explain **what you changed** and **why**.
5. Link any related issue with `Closes #123` or `Fixes #456`.

---

Thanks again for helping make rKitPvP better for everyone! 🙌
