create index IX_F00DDB17 on DepotAppCustomization (depotEntryId, ctCollectionId);
create index IX_D32F7DF6 on DepotAppCustomization (depotEntryId, enabled, ctCollectionId);
create unique index IX_2CE1592A on DepotAppCustomization (depotEntryId, portletId[$COLUMN_LENGTH:75$], ctCollectionId);

create unique index IX_E3EB2C84 on DepotEntry (groupId, ctCollectionId);
create index IX_4BFBE656 on DepotEntry (uuid_[$COLUMN_LENGTH:75$], companyId, ctCollectionId);
create index IX_6179BC8E on DepotEntry (uuid_[$COLUMN_LENGTH:75$], ctCollectionId);

create index IX_196FA5BF on DepotEntryGroupRel (ddmStructuresAvailable, toGroupId, ctCollectionId);
create index IX_8A747829 on DepotEntryGroupRel (depotEntryId, ctCollectionId);
create unique index IX_815512A2 on DepotEntryGroupRel (depotEntryId, toGroupId, ctCollectionId);
create index IX_29AD8099 on DepotEntryGroupRel (searchable, toGroupId, ctCollectionId);
create index IX_71D8BE4F on DepotEntryGroupRel (toGroupId, ctCollectionId);
create index IX_E14B1D10 on DepotEntryGroupRel (uuid_[$COLUMN_LENGTH:75$], companyId, ctCollectionId);
create index IX_AC497614 on DepotEntryGroupRel (uuid_[$COLUMN_LENGTH:75$], ctCollectionId);
create unique index IX_23B06412 on DepotEntryGroupRel (uuid_[$COLUMN_LENGTH:75$], groupId, ctCollectionId);