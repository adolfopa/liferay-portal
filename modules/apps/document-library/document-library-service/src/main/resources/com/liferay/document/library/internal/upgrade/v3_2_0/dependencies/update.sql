create table DLStorageQuota(
	mvccVersion      LONG default 0 not null,
	ctCollectionId   LONG default 0 not null,
	dlStorageQuotaId LONG           not null,
	companyId        LONG,
	storageSize      LONG,
	primary key (dlStorageQuotaId, ctCollectionId)
);

create unique index IX_4ACE7FBB on DLStorageQuota (companyId, ctCollectionId);

COMMIT_TRANSACTION;