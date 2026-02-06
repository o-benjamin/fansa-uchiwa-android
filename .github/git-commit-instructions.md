## Commit Message Guidelines

When generating a commit message, strictly follow the format below:

### Format

<branch-name>[<type>]: <short_summary_in_title_case>

* <change_detail_1>
* <change_detail_2>

### Rules

1. **Branch Name**: Use the name of the current git branch.
2. **Type**: Use one of the following:
    - feat: New features
    - fix: Bug fixes
    - refactor: Code changes that neither fix a bug nor add a feature
    - style: Changes that do not affect the meaning of the code (white-space, formatting, etc.)
    - docs: Documentation only changes
    - test: Adding missing tests or correcting existing tests
    - chore: Changes to the build process or auxiliary tools and libraries
3. **Title**: Concise summary starting with a capital letter.
4. **Body**: Use bullet points (*) to describe specific changes.
5. **Language**: Always generate the message in English.

### Example

feature/add-stamps[feat]: Add New Animal Stamps To Edit Screen

* Add cat and dog svg assets to StickerAssets.kt
* Update EditViewModel to include new categories
* Implement horizontal scroll for stamp picker