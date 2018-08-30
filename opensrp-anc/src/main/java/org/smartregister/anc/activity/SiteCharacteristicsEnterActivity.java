package org.smartregister.anc.activity;

import android.os.Bundle;
import android.view.View;

import org.smartregister.anc.R;
import org.smartregister.anc.presenter.SiteCharacteristicsPresenter;

/**
 * Created by ndegwamartin on 27/08/2018.
 */
public class SiteCharacteristicsEnterActivity extends BaseActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_characteristics_enter);

        findViewById(R.id.btn_site_characteristics).setOnClickListener(this);

        presenter = new SiteCharacteristicsPresenter(this);

    }

    @Override
    public void onClick(View view) {

        presenter.launchSiteCharacteristicsForm();
    }
}
