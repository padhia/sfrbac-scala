# sfenv

`sfenv` is an *experimental application*, a *minimum viable product* to allow declaratively define Snowflake database environments and generate SQL statements for it.

`sfenv` reads a set of *rules* from a YAML (or a JSON) file and produces SQL statements. [RULES.md](./RULES.md) describes the structure of such a file. Also, refer to [sample.yaml](./sample.yaml) for an example of a rules file.

## Features

With `sfenv` utility,

1. **Manage** following database objects
	- Databases
	- Schemas
	- Warehouses
	- Shares
	- Users
	- Application IDs
	- Access Roles that control fine-grained access to objects or resources
	- Functional Roles build on *Access Roles* for the purpose of granting permissions to User IDs
1. **Manage** privileges by following Snowflake recommended [best practices](https://community.snowflake.com/s/article/Snowflake-Security-Overview-and-Best-Practices)
	- privileges are granted to *Access Roles*
	- *Access Roles* are granted to *Functional Roles*
	- *Functional Roles* are granted to *User IDs*
1. generate incremental (delta) SQL statements for only the changes made since the last time. This greatly reduces the number of SQL statements generated and the time it takes to run them.
1. manage different permissions for different environments with one rules file.

# Usage

```sh
sfenv [--diff <filename>] [--env <string>] [--only-future] [--allow-drops] [<Rules file>]
```

Where:

- `<Rules file>`: A YAML file containing object and privileges definitions
- `--env ENV`: An *environment* name, to derive object and role names (default: `DEV`)
- `--diff <Rules file>`: Optional, when specified, SQL statements are generated only for the differences between the specified file and main rules file.
- `--drop <all|non-local|none`: determine how `DROP` SQL statements are produced
    - `all`: all `DROP` statements are produced as normal SQL statements
    - `non-local`: `DROP` statements that may lead to data loss (dropping local databases and schemas, but not imported from Shares) are commented out
    - `none`: all `DROP` statements are commented out
- `--only-future`: When generating permissions for objects at schema level, generate only `FUTURE` grants (skip `ALL` grants)

## Maintaining State

In a somewhat limited capacity, `sfenv` supports generating SQL statements relative to a previous *state*. This functionality is enabled by `--diff` option that accepts a second rules file corresponding to an earlier state of the rules file. `sfenv` can then generate SQL statements for only the differences between the two rule files. The easiest way to maintain versions of rule files is to use a version control system such as `git`.

### Easily generate incremental SQL statements

If you use `git` to manage rules files, you can define a custom [`difftool`](https://git-scm.com/docs/git-difftool) to easily generate incremental (delta) SQL statements only for the differences between two git versions.

**git configuration**

Run the following command to register `sfenv` as a `difftool`.

```sh
git config difftool.sfenv.cmd 'sfenv $REMOTE --diff $LOCAL'
```
Any time you modify the rules file, you can then use the following command to generate SQL statements to automatically process only the changes made since the previous commit

```sh
git difftool -yt sfenv my-rules.yaml
```

# Known Limitations
1. Support for only a small subset of Snowflake objects. Notably, managing Databases, Schemas, Warehouses, Roles, and permissions (RBAC) is extensively supported.
1. Error reporting may be terse, especially for YAML or JSON parsing errors. If you aren't sure if the rules file is a valid YAML or JSON file, it might be easier first to locate and fix errors by using any of the freely available online services.
1. There is no validation of object parameters or privileges. Any unrecognized object parameters or privileges are used verbatim in the generated SQL.
