# FAQ

**Is it possible to generate a rollback script in case it is needed?**

Not directly, but rolling back changes is easy, again using `git` (or any version control system). If you have the previous version of the rules file, you can generate a *rollback* script by switching current and previous versions when running `sfenv`. For example,

```sh
sfenv previous-good.yaml --diff current-bad.yaml
```

The above command should generate the SQL statements that revert the bad changes.

**Is it possible to suppress generating SQL statements for certain objects?**

Again, not directly, but you may be able to use a combination of `git` and `sfenv`'s ability to generate incremental SQL statements. For example, consider a scenario where databases and schemas were already created earlier by a separate process and you now want to manage permissions using `sfenv`. Adding new databases and schemas along with their permissions would generate `CREATE DATABASE` and/or `CREATE SCHEMA` statements along with `GRANT`/`REVOKE`. In this situation, divide the activity into two steps:

1. First, add databases and schemas to the rules file but do not generate any SQL statements
1. Next, add access roles and use the incremental SQL generation feature by supplying the file from the first step with `--diff` option
