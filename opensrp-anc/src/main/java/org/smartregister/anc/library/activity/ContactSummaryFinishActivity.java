package org.smartregister.anc.library.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.jeasy.rules.api.Facts;
import org.json.JSONObject;
import org.smartregister.anc.library.AncLibrary;
import org.smartregister.anc.library.R;
import org.smartregister.anc.library.contract.ProfileContract;
import org.smartregister.anc.library.domain.YamlConfig;
import org.smartregister.anc.library.model.PartialContact;
import org.smartregister.anc.library.presenter.ProfilePresenter;
import org.smartregister.anc.library.repository.PartialContactRepositoryHelper;
import org.smartregister.anc.library.repository.PatientRepositoryHelper;
import org.smartregister.anc.library.repository.PreviousContactRepositoryHelper;
import org.smartregister.anc.library.task.FinalizeContactTask;
import org.smartregister.anc.library.task.LoadContactSummaryDataTask;
import org.smartregister.anc.library.util.ConstantsUtils;
import org.smartregister.anc.library.util.ContactJsonFormUtils;
import org.smartregister.anc.library.util.FilePathUtils;
import org.smartregister.anc.library.util.Utils;
import org.smartregister.helper.ImageRenderHelper;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 10/07/2018.
 */
public class ContactSummaryFinishActivity extends BaseProfileActivity implements ProfileContract.View {
    private TextView nameView;
    private TextView ageView;
    private TextView gestationAgeView;
    private TextView ancIdView;
    private ImageView imageView;
    private ImageRenderHelper imageRenderHelper;
    private Facts facts = new Facts();
    private List<YamlConfig> yamlConfigList = new ArrayList<>();
    private String baseEntityId;
    private int contactNo;
    public MenuItem saveFinishMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        baseEntityId = getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID);
        contactNo = getIntent().getExtras().getInt(ConstantsUtils.IntentKeyUtils.CONTACT_NO);

        setUpViews();

        mProfilePresenter = new ProfilePresenter(this);
        imageRenderHelper = new ImageRenderHelper(this);
        loadContactSummaryData();

    }

    private void setUpViews() {
        ageView = findViewById(R.id.textview_age);
        gestationAgeView = findViewById(R.id.textview_gestation_age);
        ancIdView = findViewById(R.id.textview_anc_id);
        nameView = findViewById(R.id.textview_name);
        imageView = findViewById(R.id.imageview_profile);

        findViewById(R.id.btn_profile_registration_info).setVisibility(View.GONE);

        collapsingToolbarLayout.setTitleEnabled(false);
        if (contactNo > 0) {
            actionBar.setTitle(String.format(this.getString(R.string.contact_number),
                    getIntent().getExtras().getInt(ConstantsUtils.IntentKeyUtils.CONTACT_NO)));
        } else {
            actionBar.setTitle(R.string.back);
        }
    }

    protected void loadContactSummaryData() {
        try {
            new LoadContactSummaryDataTask(this, getIntent(), mProfilePresenter, facts, baseEntityId).execute();
        } catch (Exception e) {
            Timber.e(e, "%s loadContactSummaryData()", this.getClass().getCanonicalName());
        }
    }

    public void process() throws Exception {
        //Get actual Data
        JSONObject object;

        List<PartialContact> partialContacts = getPartialContactRepository()
                .getPartialContacts(getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID),
                        getIntent().getIntExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO, 1));

        if (partialContacts != null && !partialContacts.isEmpty()) {
            for (PartialContact partialContact : partialContacts) {
                if (partialContact.getFormJsonDraft() != null || partialContact.getFormJson() != null) {
                    object = new JSONObject(partialContact.getFormJsonDraft() != null ? partialContact.getFormJsonDraft() :
                            partialContact.getFormJson());
                    ContactJsonFormUtils.processRequiredStepsField(facts, object);
                }
            }
        }

        Iterable<Object> ruleObjects = AncLibrary.getInstance().readYaml(FilePathUtils.FileUtils.CONTACT_SUMMARY);

        yamlConfigList = new ArrayList<>();
        for (Object ruleObject : ruleObjects) {
            YamlConfig yamlConfig = (YamlConfig) ruleObject;
            yamlConfigList.add(yamlConfig);
        }
    }

    public PartialContactRepositoryHelper getPartialContactRepository() {
        return AncLibrary.getInstance().getPartialContactRepositoryHelper();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected int getViewLayoutId() {
        return R.layout.activity_contact_summary_finish;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // When user click home menu item then quit this activity.
        if (itemId == android.R.id.home) {
            PatientRepositoryHelper.updateEDDDate(baseEntityId, null); //Reset EDD
            super.onBackPressed();
        } else {
            saveFinishForm();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contact_summary_finish_activity, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProfilePresenter.onDestroy(isChangingConfigurations());
    }

    @Override
    protected void onCreation() { //Overriden from Secured Activity
    }

    @Override
    protected void onResumption() {//Overriden from Secured Activity

    }

    private void saveFinishForm() {
        new FinalizeContactTask(this, mProfilePresenter, getIntent()).execute();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        saveFinishMenuItem = menu.findItem(R.id.save_finish_menu_item);
        saveFinishMenuItem.setEnabled(false);//initially disable

        return true;
    }

    @Override
    public void setProfileName(String fullName) {
        this.womanName = fullName;
        nameView.setText(fullName);
    }

    @Override
    public void setProfileID(String ancId) {
        ancIdView.setText("ID: " + ancId);
    }

    @Override
    public void setProfileAge(String age) {
        ageView.setText("AGE " + age);

    }

    @Override
    public void setProfileGestationAge(String gestationAge) {
        gestationAgeView.setText(gestationAge != null ? "GA: " + gestationAge + " WEEKS" : "GA");
    }

    @Override
    public void setProfileImage(String baseEntityId) {
        imageRenderHelper.refreshProfileImage(baseEntityId, imageView, R.drawable.ic_woman_with_baby);
    }

    @Override
    public void setPhoneNumber(String phoneNumber) {
        //Overridden
    }

    @Override
    public void setTaskCount(String taskCount) {
        // Implement here
    }

    @Override
    public void displayToast(int stringID) {
        Utils.showShortToast(this, this.getString(stringID));
    }

    @Override
    public String getIntentString(String intentKey) {

        return getIntent().getStringExtra(intentKey);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //Overriden
    }

    protected PreviousContactRepositoryHelper getPreviousCOntactsReposity() {
        return AncLibrary.getInstance().getPreviousContactRepositoryHelper();
    }

    public List<YamlConfig> getYamlConfigList() {
        return yamlConfigList;
    }
}

