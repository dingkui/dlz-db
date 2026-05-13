# Contributing to DLZ-DB

Thank you for your interest in contributing to DLZ-DB! This document provides guidelines for contributing to the project.

## Code of Conduct

- Be respectful and constructive in all interactions
- Focus on what is best for the community
- Show empathy towards other community members

## How to Contribute

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When creating a bug report, include:

- **Title**: Clear and descriptive summary of the issue
- **Description**: Detailed explanation of the problem
- **Reproduction Steps**: Steps to reproduce the behavior
- **Expected Behavior**: What you expected to happen
- **Actual Behavior**: What actually happened
- **Environment**: 
  - DLZ-DB version
  - Java version
  - Database type and version
  - Operating system
- **Code Sample**: Minimal code sample that reproduces the issue
- **Logs**: Relevant error logs or stack traces

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When suggesting an enhancement:

- Use a clear and descriptive title
- Provide a detailed description of the proposed enhancement
- Explain the use case and why it would be useful
- Consider including example code or mockups if applicable

### Pull Requests

#### Development Workflow

1. **Fork the repository** and create your branch from `main`
2. **Make your changes** following the coding standards
3. **Write tests** for your changes
4. **Ensure all tests pass** locally
5. **Commit your changes** with clear commit messages
6. **Push to your fork** and submit a pull request

#### Commit Message Guidelines

Follow conventional commit format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

**Example:**
```
feat(query): add support for nested OR conditions

Add support for nested OR conditions using lambda expressions.
This allows complex query logic like:
.or(o -> o.like(User::getName, "keyword").like(User::getAddress, "keyword"))

Closes #123
```

#### Code Style

- Follow existing code style in the project
- Use meaningful variable and method names
- Add comments for complex logic
- Keep methods focused and concise
- Maximum line length: 120 characters

#### Testing

- Write unit tests for new features
- Write integration tests for bug fixes
- Ensure test coverage does not decrease
- All tests must pass before submitting PR

#### Pull Request Guidelines

- Fill out the PR template completely
- Link related issues
- Keep PRs focused and small if possible
- Ensure CI checks pass
- Respond to review feedback in a timely manner

## Coding Standards

### DLZ-DB Specific Guidelines

When contributing to DLZ-DB, follow these framework-specific conventions:

1. **Entry Points**: All database operations should use `DB.Pojo`, `DB.Table`, `DB.Jdbc`, or `DB.Sql`
2. **Lambda Method References**: Use method references for field names (e.g., `User::getName`)
3. **Three-Parameter Form**: Condition methods should support `(condition, field, value)` form
4. **Return Values**: Follow the naming convention:
   - `queryBean()` → Single Bean
   - `queryBeanList()` → List<Bean>
   - `queryOne()` → ResultMap
   - Write operations must end with `.execute()`
5. **Placeholders**: 
   - `DB.Jdbc` uses `?` placeholders
   - `DB.Sql` uses `#{key}` placeholders
6. **No Mapper/DAO**: Do not create Mapper interfaces or DAO classes

### Java Guidelines

- Use Java 8+ features appropriately
- Prefer immutable objects where possible
- Use Optional for nullable return values
- Follow Java naming conventions

## Documentation

- Update documentation for any API changes
- Add examples for new features
- Keep documentation in sync with code changes
- Documentation is in Chinese, but English documentation is welcome

## Getting Help

If you need help:

- Check the [documentation](./docs/)
- Search existing [issues](https://github.com/dingkui/dlz-db/issues)
- Create a new issue with the `question` label

## License

By contributing to DLZ-DB, you agree that your contributions will be licensed under the Apache License 2.0.
