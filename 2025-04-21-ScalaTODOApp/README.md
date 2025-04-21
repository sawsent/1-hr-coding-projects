# 📦 Text File-Based Task Manager (Simple Todo App)

## 📝 Project Description

Build a simple command-line task manager in Scala that lets users create, list, mark complete, and delete tasks. Tasks should persist between program runs by saving to a plain text file.

This project is designed to be completed in **about 1 hour** and requires **no external libraries or frameworks** — just the Scala standard library.

---

## 🎯 Features

- **Add a task**
  - Syntax: `ADD Buy groceries`
  - Each task gets a unique numeric ID.

- **List all tasks**
  - Syntax: `LIST`
  - Display task ID, description, and completion status.

- **Mark a task as completed**
  - Syntax: `DONE 3`
  - Mark task with ID 3 as completed.

- **Delete a task**
  - Syntax: `DEL 3`
  - Delete the task with ID 3.

- **Persist tasks to a text file**
  - On every change, save the current task list to `tasks.txt`.
  - On program start, load existing tasks from `tasks.txt` if it exists.

---

## 📑 Example Interaction

```
> ADD Buy groceries
Task added with ID 1
> ADD Call Alice
Task added with ID 2
> LIST
[1] [ ] Buy groceries
[2] [ ] Call Alice
> DONE 1
Task 1 marked as completed
> LIST
[1] [X] Buy groceries
[2] [ ] Call Alice
> DEL 2
Task 2 deleted
```

---

## 📦 Deliverables

- A **single Scala file** (e.g., `TaskManager.scala`)
- No external dependencies — should run via:
  ```
  scala TaskManager.scala
  ```

---

## 📌 Concepts You’ll Practice

- Scala collections (`List`, `Map`)
- Case classes for representing tasks
- Option types and pattern matching
- Command-line input parsing
- File I/O (read/write plain text files)
- Simple REPL (Read-Eval-Print Loop)
- Basic error handling

---

## ✳️ Bonus (if time permits)

- Add due dates or priorities for tasks.
- Display number of pending/completed tasks.
- Support saving tasks in a JSON-like format manually (without external libraries).

---

## 🕑 Estimated Completion Time

**~1 hour**

