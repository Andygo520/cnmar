package com.example.administrator.cnmar.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.example.administrator.cnmar.R;
import com.example.administrator.cnmar.activity.MaterialStockDetailActivity;
import com.example.administrator.cnmar.entity.MyListView;
import com.example.administrator.cnmar.helper.UniversalHelper;
import com.example.administrator.cnmar.helper.UrlHelper;
import com.example.administrator.cnmar.http.VolleyHelper;

import java.util.ArrayList;
import java.util.List;

import component.material.model.MaterialStock;

/**
 * A simple {@link Fragment} subclass.
 */
public class MaterialStockFragment extends Fragment {
    private MyListView lvStock;
    private LinearLayout llSearch;
    private EditText etSearchInput;
    private ImageView ivDelete;
    private MaterialRefreshLayout refreshLayout;
    private Handler handler = new Handler();
    private StockAdapter myAdapter;
    int page = 1;    //    page代表显示的是第几页内容，从1开始
    private int total; // 总页数
    private int num = 1; // 第几页
    private int count; // 数据总条数
    //    用来存放从后台取出的数据列表，作为adapter的数据源
    private List<MaterialStock> data = new ArrayList<>();
    private String url = UniversalHelper.getTokenUrl(UrlHelper.URL_STOCK.replace("{page}", String.valueOf(page)));

    public MaterialStockFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stock, container, false);
        lvStock = (MyListView) view.findViewById(R.id.stock_list_view);
        ivDelete = (ImageView) view.findViewById(R.id.ivDelete);

        refreshLayout = (MaterialRefreshLayout) view.findViewById(R.id.refresh);
        refreshLayout.autoRefresh();//drop-down refresh automatically

        refreshLayout.setLoadMore(true);
//        refreshLayout.autoRefreshLoadMore();
        refreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
                //一般加载数据都是在子线程中，这里我用到了handler
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myAdapter = null;
//                      下拉刷新默认显示第一页（10条）内容
                        page = 1;
                        getStockListFromNet(url);
                        refreshLayout.finishRefresh();
                    }
                }, 400);
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {

                if (count <= 10) {
                    refreshLayout.setLoadMore(false);
                    refreshLayout.finishRefreshLoadMore();

                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            isLoadMore = false;
                            myAdapter = new StockAdapter();
                            page++;
                            if (page == total) {
                                String strUrl = UniversalHelper.getTokenUrl(UrlHelper.URL_STOCK.replace("{page}", String.valueOf(page)));
                                Log.d("url2", strUrl);
                                getStockListFromNet(strUrl);
                                Toast.makeText(getActivity(), "加载完成", Toast.LENGTH_SHORT).show();
                                // 结束上拉刷新...
                                refreshLayout.finishRefreshLoadMore();
                                refreshLayout.setLoadMore(false);
                                return;
                            }
                            String strUrl = UniversalHelper.getTokenUrl(UrlHelper.URL_STOCK.replace("{page}", String.valueOf(page)));
                            Log.d("url2", strUrl);
                            getStockListFromNet(strUrl);
                            Toast.makeText(getActivity(), "已加载更多", Toast.LENGTH_SHORT).show();
                            // 结束上拉刷新...
                            refreshLayout.finishRefreshLoadMore();
                        }
                    }, 400);
                }
            }
        });
        llSearch = (LinearLayout) view.findViewById(R.id.llSearch);
        etSearchInput = (EditText) view.findViewById(R.id.etSearchInput);
        etSearchInput.setHint("原料编码查询");
        etSearchInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    String input = etSearchInput.getText().toString().trim();
                    if (input.equals("")) {
                        Toast.makeText(getActivity(), "请输入内容后再查询", Toast.LENGTH_SHORT).show();
                    } else {
                        String urlString = UrlHelper.URL_SEARCH_STOCK.replace("{query.code}", input);
                        urlString = UniversalHelper.getTokenUrl(urlString);
                        myAdapter = null;
                        getStockListFromNet(urlString);
                    }
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }

        });
        etSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    ivDelete.setVisibility(View.GONE);

                    myAdapter = null;
                    getStockListFromNet(url);
                } else {
                    ivDelete.setVisibility(View.VISIBLE);
                    ivDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            etSearchInput.setText("");
                        }
                    });
                }
            }
        });
        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                String input = etSearchInput.getText().toString().trim();

                if (input.equals("")) {
                    Toast.makeText(getActivity(), "请输入内容后再查询", Toast.LENGTH_SHORT).show();
                    return;
                }
                String urlString = UrlHelper.URL_SEARCH_STOCK.replace("{query.code}", input);
                urlString = UniversalHelper.getTokenUrl(urlString);
                myAdapter = null;
                getStockListFromNet(urlString);
            }
        });
//        getStockListFromNet(url);
        return view;
    }


    public void getStockListFromNet(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestQueue quene = Volley.newRequestQueue(getActivity());
                StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        String json = VolleyHelper.getJson(s);
                        component.common.model.Response response = JSON.parseObject(json, component.common.model.Response.class);
                        List<MaterialStock> list = JSON.parseArray(response.getData().toString(), MaterialStock.class);

                        count = response.getPage().getCount();
                        total = response.getPage().getTotal();
                        num = response.getPage().getNum();


                        if (myAdapter == null) {
                            data = list;
                            myAdapter = new StockAdapter(data, getActivity());
                            lvStock.setAdapter(myAdapter);
                        } else {
                            data.addAll(list);
//                            myAdapter.notifyDataSetChanged();
                            myAdapter = new StockAdapter(data, getActivity());
                            lvStock.setAdapter(myAdapter);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d("Tag", volleyError.toString());

                    }
                });
                quene.add(stringRequest);
            }
        }).start();
    }

    class StockAdapter extends BaseAdapter {
        private Context context;
        private List<MaterialStock> list = null;

        public StockAdapter(List<MaterialStock> list, Context context) {
            this.list = list;
            this.context = context;
        }

        public StockAdapter() {

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.stock_item, parent, false);
                holder.materialCode = (TextView) convertView.findViewById(R.id.code);
                holder.materialName = (TextView) convertView.findViewById(R.id.name);
                holder.stockSum = (TextView) convertView.findViewById(R.id.stockSum);
//                holder.minStock= (TextView) convertView.findViewById(R.id.minStock);
//                holder.maxStock= (TextView) convertView.findViewById(R.id.maxStock);
                holder.detail = (TextView) convertView.findViewById(R.id.detail);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();

            holder.materialCode.setText(list.get(position).getMaterial().getCode());
            holder.materialName.setText(list.get(position).getMaterial().getName());
            holder.stockSum.setText(list.get(position).getStock() + list.get(position).getMaterial().getUnit().getName());
            holder.detail.setText("详情");
            holder.detail.setTextColor(getResources().getColor(R.color.colorBase));
            holder.detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MaterialStockDetailActivity.class);
//                        intent.putExtra("ID", list.get(position).getId());
                    Bundle bundle = new Bundle();
                    bundle.putInt("ID", list.get(position).getId());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
//            }
            return convertView;
        }

        class ViewHolder {
            public TextView materialCode;
            public TextView materialName;
            public TextView stockSum;
            public TextView detail;
        }

    }


}
