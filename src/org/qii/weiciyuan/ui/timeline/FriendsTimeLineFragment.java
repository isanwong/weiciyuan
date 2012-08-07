package org.qii.weiciyuan.ui.timeline;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.dao.FriendsTimeLineMsgDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.main.PictureBitmapWorkerTask;
import org.qii.weiciyuan.ui.send.StatusNewActivity;

import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 下午12:03
 * To change this template use File | Settings | File Templates.
 */
public class FriendsTimeLineFragment extends AbstractTimeLineFragment {


    public volatile boolean isBusying = false;

    private Commander commander;

    public FriendsTimeLineFragment() {
        bean = DatabaseManager.getInstance().getHomeLineMsgList();

    }

    public static abstract class Commander {


        public void downloadAvatar(ImageView view, String url, int position, ListView listView) {

        }

        public void downContentPic(ImageView view, String url, int position, ListView listView) {

        }
    }

    public FriendsTimeLineFragment setCommander(Commander commander) {
        this.commander = commander;
        return this;
    }

    @Override
    protected void scrollToBottom() {

    }

    @Override
    public void listViewItemLongClick(AdapterView parent, View view, int position, long id) {
        view.setSelected(true);
        new MyAlertDialogFragment().setView(view).setPosition(position).show(getFragmentManager(), "");
    }

    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getStatuses().get(position));
        intent.putExtra("token", ((MainTimeLineActivity) getActivity()).getToken());
        startActivity(intent);
    }


    @Override
    protected void listViewFooterViewClick(View view) {
        if (!isBusying) {

            new FriendsTimeLineGetOlderMsgListTask(view).execute();

        }
    }

    @Override
    protected void downloadAvatar(ImageView view, String url, int position, ListView listView) {
        commander.downloadAvatar(view, url, position, listView);
    }

    @Override
    protected void downContentPic(ImageView view, String url, int position, ListView listView) {
        commander.downContentPic(view, url, position, listView);
    }


    public void refresh() {
        Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = ((MainTimeLineActivity) getActivity()).getAvatarBitmapWorkerTaskHashMap();
        Map<String, PictureBitmapWorkerTask> pictureBitmapWorkerTaskMap = ((MainTimeLineActivity) getActivity()).getPictureBitmapWorkerTaskMap();


        new FriendsTimeLineGetNewMsgListTask().execute();
        Set<String> keys = avatarBitmapWorkerTaskHashMap.keySet();
        for (String key : keys) {
            avatarBitmapWorkerTaskHashMap.get(key).cancel(true);
            avatarBitmapWorkerTaskHashMap.remove(key);
        }

        Set<String> pKeys = pictureBitmapWorkerTaskMap.keySet();
        for (String pkey : pKeys) {
            pictureBitmapWorkerTaskMap.get(pkey).cancel(true);
            pictureBitmapWorkerTaskMap.remove(pkey);
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.friendstimelinefragment_menu, menu);
        menu.add("weibo dont have messages group api");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.friendstimelinefragment_new_weibo:
                Intent intent = new Intent(getActivity(), StatusNewActivity.class);
                intent.putExtra("token", ((MainTimeLineActivity) getActivity()).getToken());
                startActivity(intent);
                break;
            case R.id.friendstimelinefragment_refresh:
                if (!isBusying) {

                    refresh();

                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    class MyAlertDialogFragment extends DialogFragment {
        View view;
        int position;

        @Override
        public void onCancel(DialogInterface dialog) {
            view.setSelected(false);
        }

        public MyAlertDialogFragment setView(View view) {
            this.view = view;
            return this;
        }

        public MyAlertDialogFragment setPosition(int position) {
            this.position = position;
            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String[] items = {"刷新", "回复"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.select))
                    .setItems(items, onClickListener);

            return builder.create();
        }

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:

                        break;
                    case 1:

                        break;
                }
            }
        };
    }


    class FriendsTimeLineGetNewMsgListTask extends AsyncTask<Void, MessageListBean, MessageListBean> {


        @Override
        protected void onPreExecute() {
            isBusying = true;

            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);

            headerView.findViewById(R.id.header_progress).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.header_text).setVisibility(View.VISIBLE);
            Animation rotateAnimation = new RotateAnimation(0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setRepeatCount(-1);
            rotateAnimation.setRepeatMode(Animation.RESTART);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            headerView.findViewById(R.id.header_progress).startAnimation(rotateAnimation);
            listView.setSelection(0);
        }

        @Override
        protected MessageListBean doInBackground(Void... params) {
            FriendsTimeLineMsgDao dao = new FriendsTimeLineMsgDao(((MainTimeLineActivity) getActivity()).getToken());
            if (getList().getStatuses().size() > 0) {
                dao.setSince_id(getList().getStatuses().get(0).getId());
            }
            MessageListBean result = dao.getGSONMsgList();
            if (result != null) {
                if (result.getStatuses().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    DatabaseManager.getInstance().addHomeLineMsg(result);
                } else {
                    DatabaseManager.getInstance().replaceHomeLineMsg(result);
                }
            }
            return result;

        }

        @Override
        protected void onPostExecute(MessageListBean newValue) {
            if (newValue != null) {
                if (newValue.getStatuses().size() == 0) {
                    Toast.makeText(getActivity(), "no new message", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), "total " + newValue.getStatuses().size() + " new messages", Toast.LENGTH_SHORT).show();
                    if (newValue.getStatuses().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                        //if position equal 0,listview don't scroll because this is the first time to refresh
                        if (position > 0)
                            position += newValue.getStatuses().size();
                        newValue.getStatuses().addAll(getList().getStatuses());
                    } else {
                        position = 0;
                    }

                    bean = newValue;
                    timeLineAdapter.notifyDataSetChanged();
                    listView.setSelectionAfterHeaderView();
                    headerView.findViewById(R.id.header_progress).clearAnimation();


                }
            }
            headerView.findViewById(R.id.header_progress).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_text).setVisibility(View.GONE);
            isBusying = false;
            if (bean.getStatuses().size() == 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            } else {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }
            super.onPostExecute(newValue);
        }
    }


    class FriendsTimeLineGetOlderMsgListTask extends AsyncTask<Void, MessageListBean, MessageListBean> {
        View footerView;

        public FriendsTimeLineGetOlderMsgListTask(View view) {
            footerView = view;
        }

        @Override
        protected void onPreExecute() {
            isBusying = true;

            ((TextView) footerView.findViewById(R.id.listview_footer)).setText("loading");
            View view = footerView.findViewById(R.id.refresh);
            view.setVisibility(View.VISIBLE);

            Animation rotateAnimation = new RotateAnimation(0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setRepeatCount(-1);
            rotateAnimation.setRepeatMode(Animation.RESTART);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            view.startAnimation(rotateAnimation);

        }

        @Override
        protected MessageListBean doInBackground(Void... params) {

            FriendsTimeLineMsgDao dao = new FriendsTimeLineMsgDao(((MainTimeLineActivity) getActivity()).getToken());
            if (getList().getStatuses().size() > 0) {
                dao.setMax_id(getList().getStatuses().get(getList().getStatuses().size() - 1).getId());
            }
            MessageListBean result = dao.getGSONMsgList();

            return result;

        }

        @Override
        protected void onPostExecute(MessageListBean newValue) {
            if (newValue != null) {
                Toast.makeText(getActivity(), "total " + newValue.getStatuses().size() + " old messages", Toast.LENGTH_SHORT).show();

                getList().getStatuses().addAll(newValue.getStatuses().subList(1, newValue.getStatuses().size() - 1));

            }

            isBusying = false;
            ((TextView) footerView.findViewById(R.id.listview_footer)).setText("click to load older message");
            footerView.findViewById(R.id.refresh).clearAnimation();
            footerView.findViewById(R.id.refresh).setVisibility(View.GONE);
            timeLineAdapter.notifyDataSetChanged();
            super.onPostExecute(newValue);
        }
    }

}

