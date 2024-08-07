package com.b502.minedroid.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.b502.minedroid.MyApplication
import com.b502.minedroid.R
import com.b502.minedroid.utils.MapManager.Difficulty
import com.b502.minedroid.utils.RecordItem

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {
    //    @Override
    //    public void onCreate(Bundle savedInstanceState) {
    //        super.onCreate(savedInstanceState);
    //        PageViewModel pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
    //        int index = 1;
    //        if (getArguments() != null) {
    //            index = getArguments().getInt(ARG_SECTION_NUMBER);
    //        }
    //        pageViewModel.setIndex(index);
    //    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_toplist, container, false)
        //        final TextView textView = root.findViewById(R.id.section_label);
        val lstv = root.findViewById<ListView>(R.id.lstv)
        var index = 1
        if (arguments != null) {
            index = arguments!!.getInt(ARG_SECTION_NUMBER)
        }

        val lst: List<RecordItem> = when (index) {
            1 -> MyApplication.Companion.Instance.sqlHelper.getRecords(Difficulty.EASY)
            2 -> MyApplication.Companion.Instance.sqlHelper.getRecords(Difficulty.MIDDLE)
            3 -> MyApplication.Companion.Instance.sqlHelper.getRecords(Difficulty.HARD)
            else -> MyApplication.Companion.Instance.sqlHelper.getRecords(Difficulty.HARD)
        }
        val arrayAdapter = ArrayAdapter(root.context, android.R.layout.simple_list_item_1, lst)
        lstv.adapter = arrayAdapter
        //        pageViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        fun newInstance(index: Int): PlaceholderFragment {
            val fragment = PlaceholderFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_SECTION_NUMBER, index)
            fragment.arguments = bundle
            return fragment
        }
    }
}