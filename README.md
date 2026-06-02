# dataNestProject

This project uses MySQL for the database backend.

## MySQL Installation

If MySQL is not installed on your development machine, install it first.

### Ubuntu / Debian

```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl enable --now mysql
```

### CentOS / RHEL / Fedora

```bash
sudo dnf install @mysql
sudo systemctl enable --now mysqld
```

### Verify MySQL installation

```bash
mysql --version
```

If you are using Oracle MySQL HeatWave as the database service, local installation is not required for connecting to the remote MySQL server.

## MySQL Database Setup

After MySQL is installed, create a `.my.cnf` file in your home directory for convenient access without typing username and password:

```bash
cat > ~/.my.cnf << 'EOF'
[client]
user=root
password=YourPassword
EOF

chmod 600 ~/.my.cnf
```

Replace `YourPassword` with your MySQL root password (if you set one).

Then run the SQL scripts to create the schema and insert sample data:

```bash
mysql < createschema.sql
mysql < initdata.sql
```

### Alternative: Without `.my.cnf`

If you prefer not to store credentials in a file, use:

```bash
mysql -u root -p < createschema.sql
mysql -u root -p < initdata.sql
```

When prompted, enter your MySQL root password.

### Remote MySQL HeatWave Instance

If you use a remote MySQL HeatWave instance, specify the host and port:

```bash
mysql -h <host> -P <port> -u <user> -p < createschema.sql
mysql -h <host> -P <port> -u <user> -p < initdata.sql
```

Or update `.my.cnf` with the remote credentials:

```
[client]
host=<host>
port=<port>
user=<user>
password=<password>
```

## Files

- `createschema.sql`: MySQL schema definition.
- `initdata.sql`: MySQL sample data inserts.
- `dropschema.sql`: Drop all tables.