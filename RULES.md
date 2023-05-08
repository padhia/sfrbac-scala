# Rules File

Rules file is a `yaml` or `json` file that is used by `sfenv` utility to generate DDLs and DCLs. A rule file contains one YAML/JSON object with several attributes (*sections*) to allow fine-grained creation of objects, roles and permissions.

Sections (or attributes of the top-level object), are documented below. Only the `config` section is required, any other section may be omitted if not needed.

- **`config`**
- `imports`
- `databases`
- `warehouses`
- `roles`
- `users`
- `apps`

Although the syntax doesn't explicitly list, but all object and role definitions accept `tags` mapping object containig a *tag* and a *value*

## `config`

A YAML/JSON object that controls naming of various database objects such as databases, schemas, roles etc. This object must have all attributes listed in the following example:

**Example**:

```yaml
config:
  secadm: "RL_{env}_SECADMIN"
  sysadm: "RL_{env}_SYSADMIN"
  database: "{db}_{env}"
  schema: "{sch}"
  warehouse: "WH_{env}_{wh}"
  acc_role: "{sch}_{acc}"
  wacc_role: "_WH_{env}_{wh}_{acc}"
  fn_role: "RL_{env}_{role}"
  app_id: "APP_{env}_{app}"
```

Notes:

- Value for each attribute is a template to derive corresponding database object name by substituting placeholder (names enclosed in `{}`).
- A special placeholder, named `env`, lets one rule file to generate different sets of SQL statements corresponding to different environments.
- Value for `env` is supplied at runtime whereas values for all other placeholder names are taken from other sections within the rules file.
- Access role names are derived using the placeholders of the resource they control.
- Generated DDLs and DCLs contain appropriate `use role ...` statements to set either `secadm` and `sysadm`, which are security and system admin roles respectively. `secadm` manages other roles and permissions, whereas `sysadm` owns all database resources in an environment.

## `imports`

A YAML/JSON object containing imported share names and their definitions.

**Example:**

```yaml
imports:
  CUST:
    provider: CUSTP
    share: DATA_SHR
    roles:
      - DBA
      - DEVLOPER
```
### imported share
An imported share is a YAML/JSON object that has following attributes:

- **`provider`**: provider account name
- **`share`**: name of the share
- `roles`: A list of functional roles that will be granted `imported privileges`JSON objects

## `databases`

A YAML/JSON object containing database names and their definitions.

**Example:**

```yaml
databases:
  EDW:
    data_retention_time_in_days: 10
    comment: EDW core database
    schemas:
      CUSTOMER: &sch_defaults
        managed: true
        data_retention_time_in_days: 10
        acc_roles:
          R:
            database: [usage, monitor]
            schema: [usage, monitor]
            table: [select, references]
            view: [select]
          RW:
            role: [R]
            table: [insert, update, truncate, delete]
          RWC:
            role: [RW]
            schema: ["create table", "create view", "create procedure"]

  BI:
    transient: true
    data_retention_time_in_days: 10
    comment: Analytics database
    schemas:
      CUSTOMER:
      	<<: *sch_defaults
        transient: true
```

### database
A database is a YAML/JSON object that has following attributes:

- `transient`: `true` if this is a transient database
- `comment`: comment that'll be part of the generated DDL
- `schemas`: A YAML/JSON list containing one or more schema YAML/JSON objects
- `...`: Any other attributes are generated as part of the DDL

### schema
A schema is a YAML/JSON object that defines a database schema and has following attributes:

- `transient`: `true` if this is a transient schema
- `managed`: `true` if this is a managed schema
- `comment`: comment that'll be part of the generated DDL
- `acc_roles`: A YAML/JSON object containing access role definitions to create
- `...`: Any other attributes are generated as part of the DDL

## `warehouses`

`warehouses` is a YAML/JSON object containing one or more warehouse names and definitions

**Example**

```yaml
LOAD: &wh_defaults
  warehouse_size: SMALL
  initially_suspended: true
  auto_suspend: 300
  auto_resume: true
  acc_roles:
    R:
      warehouse: [usage]
    RW:
      role: [R]
      warehouse: [operate]
    RWC:
      role: [RW]
      warehouse: [monitor, modify]

ETL:
  <<: *wh_defaults
  warehouse_size: X-LARGE
```

### warehouse

A warehouse is a YAML/JSON object with following attributes:
- `comment`: comment that'll be part of the generated DDL
- `acc_roles`: A YAML/JSON object containing access role definitions to create
- `...`: Any other attributes are generated as part of the DDL

## access roles
An access role is created for either a schema or a warehouse. An access role is specified as a YAML/JSON object with access role name as key and attribute being another YAML/JSON object that encodes a list of permissions for each database object type.

**Example**

```yaml
R:
  database: [usage, monitor]
  schema: [usage, monitor]
  table: [select, references]
  view: [select]
RW:
  role: [R]
  table: [insert, update, truncate, delete]
```

## `roles`
A YAML/JSON object containing one or more *functional role* names and their definitions

### functional role

A functional role has following attributes:
- `comment`: comment that'll be part of the generated DDL
- `acc_roles`: A YAML/JSON list containing *references* to a mix of schema or warehouse access roles
- `env_acc_roles`: Allows specifying access role references per environment

### access role reference
An access role reference is a YAML/JSON object that has one of the following set of attributes:

**Option 1**
- `database`: database name
- `schema`: schema name
- `acc`: access role name

**Option 2**
- `warehouse`: warehouse name
- `acc`: access role name

## `users`
A YAML/JSON object containing one or more *user* names and their definitions

### user
A YAML/JSON object with following attributes:
- `comment`: comment that'll be part of the generated DDL
- `roles`: A YAML/JSON list containing names of the functional roles to be assigned to this user ID

## `apps`
A YAML/JSON object, very similar to `users`, except the IDs are created per environment.
