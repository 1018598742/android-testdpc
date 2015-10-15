/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.testdpc.common;

import android.app.Fragment;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.android.testdpc.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This fragment shows a spinner of all allowed apps and a list of properties associated with the
 * currently selected application.
 */
public abstract class ManageAppFragment extends Fragment {

    protected PackageManager mPackageManager;
    protected Spinner mManagedAppsSpinner;
    protected ListView mAppListView;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.manage_apps);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPackageManager = getActivity().getPackageManager();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.manage_apps, null);

        List<ApplicationInfo> managedAppList = getInstalledLaunchableApps();
        Collections.sort(managedAppList,
                new ApplicationInfo.DisplayNameComparator(mPackageManager));
        AppInfoSpinnerAdapter appInfoSpinnerAdapter = new AppInfoSpinnerAdapter(getActivity(),
                R.layout.app_row, R.id.pkg_name, managedAppList);
        mManagedAppsSpinner = (Spinner) view.findViewById(R.id.managed_apps_list);
        mManagedAppsSpinner.setAdapter(appInfoSpinnerAdapter);
        mManagedAppsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadData(((ApplicationInfo) mManagedAppsSpinner.getSelectedItem()).packageName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });
        mAppListView = (ListView) view.findViewById(R.id.app_list_view);
        loadData(((ApplicationInfo) mManagedAppsSpinner.getSelectedItem()).packageName);
        return view;
    }

    private List<ApplicationInfo> getInstalledLaunchableApps() {
        List<ApplicationInfo> managedAppList = mPackageManager.getInstalledApplications(
                0 /* Default flags */);
        List<ApplicationInfo> launchableAppList = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo applicationInfo : managedAppList) {
            if ((mPackageManager.getLaunchIntentForPackage(applicationInfo.packageName)) != null) {
                launchableAppList.add(applicationInfo);
            }
        }
        return launchableAppList;
    }

    /**
     * Populates the adapter for app_list_view with data for this application.
     * @param pkgName The package for which to load information
     */
    protected abstract void loadData(String pkgName);

}
