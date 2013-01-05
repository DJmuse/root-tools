package com.rarnu.tools.root.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Loader;
import android.content.Loader.OnLoadCompleteListener;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.rarnu.tools.root.R;
import com.rarnu.tools.root.adapter.HostsAdapter;
import com.rarnu.tools.root.api.LogApi;
import com.rarnu.tools.root.base.BasePopupFragment;
import com.rarnu.tools.root.common.HostRecordInfo;
import com.rarnu.tools.root.common.MenuItemIds;
import com.rarnu.tools.root.comp.DataProgressBar;
import com.rarnu.tools.root.loader.HostsLoader;
import com.rarnu.tools.root.utils.DIPairUtils;
import com.rarnu.tools.root.utils.PingUtils;

public class HostDeprecatedFragment extends BasePopupFragment implements
		OnLoadCompleteListener<List<HostRecordInfo>> {

	ListView lvDeprecatedHosts;
	DataProgressBar progressDeprecated;

	List<HostRecordInfo> lstDeprecated = new ArrayList<HostRecordInfo>();
	HostsAdapter adapter = null;

	HostsLoader loader = null;
	MenuItem itemScan;

	private Handler hSelectHost = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		};

	};

	@Override
	public void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogApi.logEnterDeprecatedHosts();
	};

	@Override
	protected int getBarTitle() {
		return R.string.clean_deprecated_hosts;
	}

	@Override
	protected int getBarTitleWithPath() {
		return R.string.clean_deprecated_hosts;
	}

	@Override
	protected void initComponents() {
		lvDeprecatedHosts = (ListView) innerView
				.findViewById(R.id.lvDeprecatedHosts);
		progressDeprecated = (DataProgressBar) innerView
				.findViewById(R.id.progressDeprecated);
		adapter = new HostsAdapter(getActivity().getLayoutInflater(),
				lstDeprecated, hSelectHost, false, false);
		lvDeprecatedHosts.setAdapter(adapter);
		loader = new HostsLoader(getActivity());
		loader.registerListener(0, this);
	}

	@Override
	protected void initLogic() {
		doStartLoad();

	}

	private void doStartLoad() {
		progressDeprecated.setAppName(getString(R.string.loading));
		progressDeprecated.setVisibility(View.VISIBLE);
		
		loader.startLoading();
	}

	@Override
	protected int getFragmentLayoutResId() {
		return R.layout.layout_host_deprecated;
	}

	@Override
	protected void initMenu(Menu menu) {
		itemScan = menu.add(0, MenuItemIds.MENU_SCAN, 99, R.string.scan);
		itemScan.setIcon(android.R.drawable.ic_menu_manage);
		itemScan.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MenuItemIds.MENU_SCAN:
			scanDeprecatedHostsT();
			break;
		}
		return true;
	}

	private void scanDeprecatedHostsT() {
		LogApi.logCleanDeprecatedHosts();
		progressDeprecated.setAppName(getString(R.string.testing));
		progressDeprecated.setVisibility(View.VISIBLE);
		itemScan.setEnabled(false);

		final Handler h = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				if (msg.what == 1) {
					progressDeprecated.setVisibility(View.GONE);
					itemScan.setEnabled(true);
					adapter.notifyDataSetChanged();

					boolean ret = DIPairUtils.saveHosts(lstDeprecated);
					if (ret) {
						Toast.makeText(getActivity(), R.string.save_hosts_succ,
								Toast.LENGTH_LONG).show();
						getActivity().finish();
					} else {
						Toast.makeText(getActivity(),
								R.string.save_hosts_error, Toast.LENGTH_LONG)
								.show();
					}
				} else if (msg.what == 2) {
					progressDeprecated.setProgress((String) msg.obj);
				}
				super.handleMessage(msg);
			}
		};

		new Thread(new Runnable() {

			@Override
			public void run() {
				String ping = "";
				int count = lstDeprecated.size();
				for (int i = count - 1; i >= 0; i--) {
					Message msg = new Message();
					msg.what = 2;
					msg.obj = lstDeprecated.get(i).ip;
					h.sendMessage(msg);

					ping = PingUtils.ping(lstDeprecated.get(i).ip);
					if (ping.equals("") || ping.equals("timeout")) {
						lstDeprecated.remove(i);
					}
				}
				h.sendEmptyMessage(1);
			}
		}).start();

	}

	@Override
	public void onLoadComplete(Loader<List<HostRecordInfo>> loader,
			List<HostRecordInfo> data) {
		lstDeprecated.clear();
		if (data != null) {
			lstDeprecated.addAll(data);
		}
		adapter.setNewData(lstDeprecated);
		progressDeprecated.setVisibility(View.GONE);

	}

}
