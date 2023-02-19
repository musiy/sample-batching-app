-- script to clear data in BATCH_* tables. Could be used in local debug.
delete from BATCH_STEP_EXECUTION_CONTEXT where 1=1;
delete from BATCH_JOB_EXECUTION_CONTEXT where 1=1;
delete from BATCH_STEP_EXECUTION where 1=1;
delete from BATCH_JOB_EXECUTION_PARAMS where 1=1;
delete from BATCH_JOB_EXECUTION where 1=1;
delete from BATCH_JOB_INSTANCE where 1=1;
commit ;