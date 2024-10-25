# sfenv

`sfenv` helps manage *Snowflake Environments* consisting of Snowflake objects and permissions.

Snowflake objects and permission are declaratively defined in a YAML/JSON file (called *Rules File*). See [sample.yaml](./sample.yaml) for an example and [RULES.md](./RULES.md) for a detailed description of rules specification.

## Features

With `sfenv` utility,

1. A single rule file can be used to manage one or more *environments*
1. The following database objects are supported
	- Databases
	- Schemas
	- Warehouses
	- Shares
	- Users
	- Application IDs
	- Access Roles that control fine-grained access to objects or resources
	- Functional Roles build on *Access Roles* to grant permissions to User IDs
1. *Privileges* can be defined as per Snowflake's [recommended best practices](https://community.snowflake.com/s/article/Snowflake-Security-Overview-and-Best-Practices)
	- privileges are granted to *Access Roles* (access roles are database roles when they apply to database objects)
	- *Access Roles* are granted to *Functional Roles*
	- *Functional Roles* are granted to *User IDs*
1. When the Rules-file is controlled by git, it is possible to generate incremental (delta) SQL statements for only the changes made since the last time (see [Maintaining State](#maintaining-state) below)
1. Customize permissions for specific environments within one rules file.

# Usage

```sh
sfenv [--diff <Rules-file>] [--env <string>] [--only-future] [--allow-drops] [<Rules-file>]
```

Where:

- `<Rules-file>`: A YAML/JSON file containing object and privileges definitions
- `--env ENV`: An *environment* name, to derive object and role names (default: `DEV`)
- `--diff <Rules file>`: Optional, when specified, SQL statements are generated only for the differences between the specified file and main rules file.
- `--drop <all|non-local|none`: determine how the `DROP` SQL statements are generated
	- `all`: all `DROP` statements are produced as normal SQL statements
	- `non-local`: `DROP` statements that may lead to data loss (includes local databases and schemas, but not the *shared* databases) are commented out
	- `none`: all `DROP` statements are commented out
- `--only-future`: When generating permissions for objects at the schema level, generate only `FUTURE` grants, and skip `ALL` grants which can be expensive to run

## Maintaining State

In a somewhat limited capacity, `sfenv` supports generating SQL statements relative to a previous *state*. This functionality is enabled by `--diff` option that accepts a second rules file corresponding to an earlier state of the rules file. `sfenv` can then generate SQL statements for only the differences between the two rule files. The easiest way to maintain versions of rule files is to use a version control system such as `git`.

### Easily generate incremental SQL statements

If you use `git` to manage rules files, you can define a custom [`difftool`](https://git-scm.com/docs/git-difftool) to easily generate incremental (delta) SQL statements only for the differences between two git versions.

**git configuration**

Run the following command to register `sfenv` as a `difftool`.

```sh
git config difftool.sfenv.cmd 'sfenv $REMOTE --diff $LOCAL'
```
Any time you modify the rules file, you can then use the following command to generate SQL statements to process only the changes made since the previous commit automatically

```sh
git difftool -yt sfenv my-rules.yaml
```

# Known Limitations
1. Does not support all available Snowflake objects. Specifically, managing Databases, Schemas, Warehouses, Roles, Users and permissions (RBAC) are fully supported.
1. Error reporting may be terse, especially for YAML or JSON parsing errors. If you aren't sure if the rules file is a valid YAML or JSON file, it might be easier first to locate and fix mistakes by using any of the freely available online services.
1. There is no validation of object parameters or privileges. Any unrecognized object parameters or privileges are used verbatim in the generated SQL.
