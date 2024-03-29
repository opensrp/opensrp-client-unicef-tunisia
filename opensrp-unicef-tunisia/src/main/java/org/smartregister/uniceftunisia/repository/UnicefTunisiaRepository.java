package org.smartregister.uniceftunisia.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.AllConstants;
import org.smartregister.child.util.ChildDbMigrations;
import org.smartregister.child.util.Utils;
import org.smartregister.configurableviews.repository.ConfigurableViewsRepository;
import org.smartregister.domain.db.Column;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.HeightZScoreRepository;
import org.smartregister.growthmonitoring.repository.WeightForHeightRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.repository.WeightZScoreRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.reporting.ReportingLibrary;
import org.smartregister.reporting.repository.DailyIndicatorCountRepository;
import org.smartregister.reporting.repository.IndicatorQueryRepository;
import org.smartregister.reporting.repository.IndicatorRepository;
import org.smartregister.repository.AlertRepository;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Hia2ReportRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.LocationTagRepository;
import org.smartregister.repository.PlanDefinitionRepository;
import org.smartregister.repository.PlanDefinitionSearchRepository;
import org.smartregister.repository.Repository;
import org.smartregister.repository.SettingsRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.uniceftunisia.BuildConfig;
import org.smartregister.uniceftunisia.application.UnicefTunisiaApplication;
import org.smartregister.uniceftunisia.reporting.annual.coverage.repository.AnnualReportRepository;
import org.smartregister.uniceftunisia.reporting.annual.coverage.repository.VaccineCoverageTargetRepository;
import org.smartregister.uniceftunisia.reporting.indicatorposition.IndicatorPositionRepository;
import org.smartregister.uniceftunisia.reporting.monthly.MonthlyReportsRepository;
import org.smartregister.uniceftunisia.util.AppConstants;
import org.smartregister.util.DatabaseMigrationUtils;

import java.util.ArrayList;

import timber.log.Timber;

public class UnicefTunisiaRepository extends Repository {

    private SQLiteDatabase readableDatabase;
    private SQLiteDatabase writableDatabase;

    public UnicefTunisiaRepository(@NonNull Context context, @NonNull org.smartregister.Context openSRPContext) {
        super(context, AllConstants.DATABASE_NAME, BuildConfig.DATABASE_VERSION, openSRPContext.session(),
                UnicefTunisiaApplication.createCommonFtsObject(context), openSRPContext.sharedRepositoriesArray());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        EventClientRepository.createTable(database, EventClientRepository.Table.client, EventClientRepository.client_column.values());
        EventClientRepository.createTable(database, EventClientRepository.Table.event, EventClientRepository.event_column.values());
        ConfigurableViewsRepository.createTable(database);
        UniqueIdRepository.createTable(database);
        SettingsRepository.onUpgrade(database);
        WeightRepository.createTable(database);
        HeightRepository.createTable(database);
        VaccineRepository.createTable(database);
        WeightForHeightRepository.createTable(database);
        ClientRegisterTypeRepository.createTable(database);
        ChildAlertUpdatedRepository.createTable(database);

        //reporting
        IndicatorRepository.createTable(database);
        IndicatorQueryRepository.createTable(database);
        DailyIndicatorCountRepository.createTable(database);
        MonthlyReportsRepository.getInstance().createTable(database);
        IndicatorPositionRepository.getInstance().createTable(database);
        VaccineCoverageTargetRepository.getInstance().createTable(database);
        AnnualReportRepository.getInstance().createTable(database);

        LocationRepository.createTable(database);
        LocationTagRepository.createTable(database);

        runLegacyUpgrades(database);

        onUpgrade(database, 11, BuildConfig.DATABASE_VERSION);

        // initialize from yml file
        ReportingLibrary reportingLibraryInstance = ReportingLibrary.getInstance();
        // Check if indicator data initialised
        boolean indicatorDataInitialised = Boolean.parseBoolean(reportingLibraryInstance.getContext()
                .allSharedPreferences().getPreference(AppConstants.Pref.INDICATOR_DATA_INITIALISED));
        boolean isUpdated = checkIfAppUpdated();
        if (!indicatorDataInitialised || isUpdated) {
            Timber.d("Initialising indicator repositories!!");
            reportingLibraryInstance.initIndicatorData(AppConstants.File.INDICATOR_CONFIG_FILE, database); // This will persist the data in the DB
            reportingLibraryInstance.getContext().allSharedPreferences().savePreference(AppConstants.Pref.INDICATOR_DATA_INITIALISED, "true");
            reportingLibraryInstance.getContext().allSharedPreferences().savePreference(AppConstants.Pref.APP_VERSION_CODE, String.valueOf(BuildConfig.VERSION_CODE));
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.w("Upgrading database from version %d to %d, which will destroy all old data", oldVersion, newVersion);

        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 2:
                    upgradeToVersion2(db);
                    break;
                case 3:
                    upgradeToVersion3(db);
                    break;
                case 4:
                    upgradeToVersion4(db);
                    break;
                case 6:
                    upgradeToVersion6(db);
                    break;
                case 7:
                    upgradeToVersion7OutOfArea(db);
                    break;
                case 9:
                    ChildDbMigrations.addShowBcg2ReminderAndBcgScarColumnsToEcChildDetails(db);
                    break;
                case 12:
                    PlanDefinitionRepository.createTable(db);
                    PlanDefinitionSearchRepository.createTable(db);
                    break;
                case 14:
                    upgradeToVersion14(db);
                case 15:
                    upgradeToVersion15(db);
                default:
                    break;
            }
            upgradeTo++;
        }

        ChildDbMigrations.addShowBcg2ReminderAndBcgScarColumnsToEcChildDetails(db);

        IndicatorQueryRepository.performMigrations(db);

        IndicatorPositionRepository.getInstance().populateIndicatorPosition(db, oldVersion, newVersion);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        byte[] pass = UnicefTunisiaApplication.getInstance().getPassword();
        if (pass != null && pass.length > 0) {
            return getReadableDatabase(pass);
        }
        return null;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        byte[] pass = UnicefTunisiaApplication.getInstance().getPassword();
        if (pass != null && pass.length > 0) {
            return getWritableDatabase(pass);
        } else {
            throw new IllegalStateException("Password is blank");
        }
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase(String password) {
        if (writableDatabase == null || !writableDatabase.isOpen()) {
            if (writableDatabase != null) {
                writableDatabase.close();
            }
            writableDatabase = super.getWritableDatabase(password);
        }
        return writableDatabase;
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase(String password) {
        try {
            if (readableDatabase == null || !readableDatabase.isOpen()) {
                if (readableDatabase != null) {
                    readableDatabase.close();
                }
                readableDatabase = super.getReadableDatabase(password);
            }
            return readableDatabase;
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    @Override
    public synchronized void close() {
        if (readableDatabase != null) {
            readableDatabase.close();
        }

        if (writableDatabase != null) {
            writableDatabase.close();
        }
        super.close();
    }

    private void runLegacyUpgrades(@NonNull SQLiteDatabase database) {
        upgradeToVersion2(database);
        upgradeToVersion3(database);
        upgradeToVersion4(database);
        upgradeToVersion6(database);
        upgradeToVersion7OutOfArea(database);
        upgradeToVersion7UpgradeTables(database);
        upgradeToVersion7RemoveUnnecessaryTables(database);
    }

    /**
     * Version 2 added some columns to the ec_child table
     *
     * @param database database
     */
    private void upgradeToVersion2(@NonNull SQLiteDatabase database) {
        try {
            // Run insert query
            ArrayList<String> newlyAddedFields = new ArrayList<>();
            newlyAddedFields.add("BCG_2");
            newlyAddedFields.add("inactive");
            newlyAddedFields.add("lost_to_follow_up");

            DatabaseMigrationUtils.addFieldsToFTSTable(database, commonFtsObject, Utils.metadata().childRegister.tableName,
                    newlyAddedFields);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion2");
        }
    }

    private void upgradeToVersion3(@NonNull SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(VaccineRepository.EVENT_ID_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(WeightRepository.EVENT_ID_INDEX);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(HeightRepository.EVENT_ID_INDEX);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(VaccineRepository.FORMSUBMISSION_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(WeightRepository.FORMSUBMISSION_INDEX);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(HeightRepository.FORMSUBMISSION_INDEX);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion3");
        }
    }

    private void upgradeToVersion4(@NonNull SQLiteDatabase db) {
        try {
            db.execSQL(AlertRepository.ALTER_ADD_OFFLINE_COLUMN);
            db.execSQL(AlertRepository.OFFLINE_INDEX);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion4");
        }
    }

    private void upgradeToVersion6(@NonNull SQLiteDatabase db) {
        try {
            WeightZScoreRepository.createTable(db);
            db.execSQL(WeightRepository.ALTER_ADD_Z_SCORE_COLUMN);

            HeightZScoreRepository.createTable(db);
            db.execSQL(HeightRepository.ALTER_ADD_Z_SCORE_COLUMN);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion6");
        }
    }

    private void upgradeToVersion7OutOfArea(@NonNull SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_HIA2_STATUS_COL);

        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion7");
        }
    }

    private void upgradeToVersion7UpgradeTables(@NonNull SQLiteDatabase db) {
        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);

            db.execSQL(WeightRepository.ALTER_ADD_CREATED_AT_COLUMN);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_TEAM_COL);
            db.execSQL(WeightRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            WeightRepository.migrateCreatedAt(db);

            db.execSQL(HeightRepository.ALTER_ADD_CREATED_AT_COLUMN);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_TEAM_COL);
            db.execSQL(HeightRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            HeightRepository.migrateCreatedAt(db);

            db.execSQL(VaccineRepository.ALTER_ADD_CREATED_AT_COLUMN);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            VaccineRepository.migrateCreatedAt(db);

        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion7UpgradeTables");
        }
    }

    private void upgradeToVersion7RemoveUnnecessaryTables(@NonNull SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS address");
            db.execSQL("DROP TABLE IF EXISTS obs");
            if (DatabaseMigrationUtils.isColumnExists(db, "path_reports", Hia2ReportRepository.report_column.json.name()))
                db.execSQL("ALTER TABLE path_reports RENAME TO " + Hia2ReportRepository.Table.hia2_report.name() + ";");
            if (DatabaseMigrationUtils.isColumnExists(db, EventClientRepository.Table.client.name(), "firstName"))
                DatabaseMigrationUtils.recreateSyncTableWithExistingColumnsOnly(db, EventClientRepository.Table.client);
            if (DatabaseMigrationUtils.isColumnExists(db, EventClientRepository.Table.event.name(), "locationId"))
                DatabaseMigrationUtils.recreateSyncTableWithExistingColumnsOnly(db, EventClientRepository.Table.event);

        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion7RemoveUnnecessaryTables");
        }
    }

    private boolean checkIfAppUpdated() {
        String appVersionCodePref = AppConstants.Pref.APP_VERSION_CODE;
        String savedAppVersion = ReportingLibrary.getInstance().getContext().allSharedPreferences().getPreference(appVersionCodePref);
        if (savedAppVersion.isEmpty()) {
            return true;
        } else {
            int savedVersion = Integer.parseInt(savedAppVersion);
            return (BuildConfig.VERSION_CODE > savedVersion);
        }
    }

    private void upgradeToVersion14(SQLiteDatabase db) {
        db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_IS_VOIDED_COL);
        db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_IS_VOIDED_COL_INDEX);
    }

    private void upgradeToVersion15(SQLiteDatabase db) {
        try{
            AllSharedPreferences sharedPreferences = UnicefTunisiaApplication.getInstance().context().userService()
                    .getAllSharedPreferences();
            db.execSQL(VaccineRepository.UPDATE_TABLE_VACCINES_ADD_OUTREACH_COL);
            db.execSQL(VaccineRepository.UPDATE_OUTREACH_QUERRY, new String[]{sharedPreferences.fetchDefaultLocalityId(sharedPreferences.fetchPioneerUser())});

        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion15");
        }
    }
}
