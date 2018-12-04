package org.blackdread.sqltojava.repository;

import org.blackdread.sqltojava.jooq.tables.Columns;
import org.blackdread.sqltojava.jooq.tables.TableConstraints;
import org.blackdread.sqltojava.pojo.ColumnInformation;
import org.blackdread.sqltojava.pojo.TableInformation;
import org.blackdread.sqltojava.pojo.TableRelationInformation;
import org.blackdread.sqltojava.jooq.InformationSchema;
import org.blackdread.sqltojava.pojo.ColumnInformation;
import org.blackdread.sqltojava.pojo.TableInformation;
import org.blackdread.sqltojava.pojo.TableRelationInformation;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.blackdread.sqltojava.jooq.InformationSchema.INFORMATION_SCHEMA;
import static org.blackdread.sqltojava.jooq.tables.KeyColumnUsage.KEY_COLUMN_USAGE;
import static org.blackdread.sqltojava.jooq.tables.TableConstraints.TABLE_CONSTRAINTS;
import static org.blackdread.sqltojava.jooq.tables.ConstraintColumnUsage.CONSTRAINT_COLUMN_USAGE;
import static org.blackdread.sqltojava.jooq.tables.Columns.COLUMNS;



import org.blackdread.sqltojava.jooq.InformationSchema.*;
/**
 * <p>Created on 2018/2/8.</p>
 *
 * @author Yoann CAPLAIN
 */
@Repository
public class InformationSchemaRepository {

    private static final Logger log = LoggerFactory.getLogger(InformationSchemaRepository.class);

    private final DSLContext create;

    @Autowired
    public InformationSchemaRepository(final DSLContext create) {
        this.create = create;
    }


    public List<TableRelationInformation> getAllTableRelationInformation(final String dbName) {


        return create.select(TABLE_CONSTRAINTS.TABLE_NAME,
            KEY_COLUMN_USAGE.COLUMN_NAME,
            CONSTRAINT_COLUMN_USAGE.TABLE_NAME,
            CONSTRAINT_COLUMN_USAGE.COLUMN_NAME)
            .from(InformationSchema.INFORMATION_SCHEMA.TABLE_CONSTRAINTS)
            .join(InformationSchema.INFORMATION_SCHEMA.KEY_COLUMN_USAGE)
            .on(TABLE_CONSTRAINTS.CONSTRAINT_NAME.eq(KEY_COLUMN_USAGE.CONSTRAINT_NAME))
            .join(InformationSchema.INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE)
            .on(CONSTRAINT_COLUMN_USAGE.CONSTRAINT_NAME.eq(TABLE_CONSTRAINTS.CONSTRAINT_NAME))
            .where(TABLE_CONSTRAINTS.CONSTRAINT_TYPE.eq("FOREIGN KEY")
                .and(CONSTRAINT_COLUMN_USAGE.TABLE_SCHEMA.eq(dbName))
                .and(CONSTRAINT_COLUMN_USAGE.TABLE_NAME.isNotNull()))
            .orderBy(TABLE_CONSTRAINTS.TABLE_NAME,KEY_COLUMN_USAGE.COLUMN_NAME)
            .fetch()
            .map(this::map);


    }

    public List<ColumnInformation> getFullColumnInformationOfTable(final String dbName, final String tableName) {

        /*  select * from information_schema.columns where table_schema='sybase' and table_name='lex_type_completion';*/

        return create.selectDistinct(
            COLUMNS.COLUMN_NAME,
            COLUMNS.DATA_TYPE,
            COLUMNS.COLLATION_NAME,
            COLUMNS.IS_NULLABLE,
            TABLE_CONSTRAINTS.CONSTRAINT_TYPE,
            COLUMNS.COLUMN_DEFAULT,
            COLUMNS.IS_GENERATED,
            COLUMNS.CHARACTER_MAXIMUM_LENGTH
        )
            .from(InformationSchema.INFORMATION_SCHEMA.COLUMNS)
            .leftJoin(InformationSchema.INFORMATION_SCHEMA.KEY_COLUMN_USAGE)
            .on(KEY_COLUMN_USAGE.COLUMN_NAME.eq(COLUMNS.COLUMN_NAME)
                .and(KEY_COLUMN_USAGE.TABLE_NAME.eq(COLUMNS.TABLE_NAME)))
            .leftJoin(TABLE_CONSTRAINTS)
            .on(TABLE_CONSTRAINTS.CONSTRAINT_NAME.eq(KEY_COLUMN_USAGE.CONSTRAINT_NAME))
            .where(COLUMNS.TABLE_SCHEMA.eq(dbName).and(COLUMNS.TABLE_NAME.eq(tableName))
            )
            .fetch()
            .map( r -> {
                String contraintType=((String) r.get("constraint_type"));
                String realContraintType = ( contraintType != null && contraintType.equals("PRIMARY KEY") ) ? "PRI" : "";
                String dataType=(String) r.get("data_type");
                String realDataType = dataType.equals("character varying")
                    ? "CHAR(" + (r.get("character_maximum_length")) + ")"
                    : dataType;

                return new ColumnInformation((String) r.get("column_name"),
                    realDataType,
                    (String) r.get("collation_name"),
                    (String) r.get("is_nullable"),
                    realContraintType,
                    (String) r.get("column_default"),
                    (String) r.get("is_generated"),
                    "");
            });

    }

    public List<TableInformation> getAllTableInformation(final String dbName) {
        return create.select(
            InformationSchema.INFORMATION_SCHEMA.TABLES.TABLE_NAME,
            InformationSchema.INFORMATION_SCHEMA.TABLES.TABLE_NAME)//TODO add comment from table if possible
            .from(InformationSchema.INFORMATION_SCHEMA.TABLES)
            .where(InformationSchema.INFORMATION_SCHEMA.TABLES.TABLE_SCHEMA.eq(dbName))
            .fetch()
            .map(r -> new TableInformation(r.value1(), r.value2()));


    }

    public List<String> getAllTableName(final String dbName) {
        return create.select(
            InformationSchema.INFORMATION_SCHEMA.TABLES.TABLE_NAME)
            .from(InformationSchema.INFORMATION_SCHEMA.TABLES)
            .where(InformationSchema.INFORMATION_SCHEMA.TABLES.TABLE_SCHEMA.eq(dbName))
            .fetch()
            .getValues(InformationSchema.INFORMATION_SCHEMA.TABLES.TABLE_NAME);
    }

    private TableRelationInformation map(final Record4<String, String, String, String> r) {
        return new TableRelationInformation(r.value1(), r.value2(), r.value3(), r.value4());
    }
}
