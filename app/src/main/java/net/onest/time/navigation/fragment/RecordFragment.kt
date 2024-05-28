package net.onest.time.navigation.fragment

import android.annotation.SuppressLint
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import net.onest.time.R
import net.onest.time.api.StatisticApi
import net.onest.time.api.vo.statistic.StatisticVo
import net.onest.time.databinding.RecordFragmentBinding
import net.onest.time.utils.ColorUtil
import net.onest.time.utils.DateUtil
import net.onest.time.utils.showToast
import java.util.SortedMap
import java.util.TreeMap

class RecordFragment : Fragment() {
    private var i = 0

    //饼状图:
    private var pieChart: PieChart? = null

    //选择日期：
    private var radioDataGroup: RadioGroup? = null
    private var radioDayButton: RadioButton? = null
    private var radioWeekButton: RadioButton? = null
    private var radioMonthButton: RadioButton? = null

    private var todayFocus: TextView? = null
    private var dataDateTxt: TextView? = null
    private var appDateTxt: TextView? = null

    //水平柱状图:
    private var barHor: HorizontalBarChart? = null
    var list: MutableList<BarEntry> = ArrayList()

    private lateinit var binding: RecordFragmentBinding

    private var statisticsVo: StatisticVo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            statisticsVo = StatisticApi.statistic()
        } catch (e: RuntimeException) {
            e.message?.showToast()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RecordFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        findViews(view)
        setListeners()
        return view
    }

    private fun setListeners() {
        // 专注时长分布
        // 左按钮
        binding.focusDurationRatioRight.setOnClickListener {

        }

        // 右按钮
        binding.focusDurationRatioRight.setOnClickListener {

        }

        // 分享按钮
        binding.focusDurationRatioShare.setOnClickListener {

        }

        // 日 周 月 按钮
        radioDataGroup!!.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.record_fragment_time_data_day -> {
                    val pieEntries = statisticsVo!!.ratioByDurationOfDay.map {
                        PieEntry(it.ratio.toFloat(), it.taskName)
                    }

                    val colors = statisticsVo!!.ratioByDurationOfDay.map {
                        ColorUtil.getColorByRgb(null)
                    }

                    dataDateTxt!!.text = DateUtil.curDay
                    setPieChartData(pieEntries, colors)
                    pieChart!!.notifyDataSetChanged()
                }

                R.id.record_fragment_time_data_week -> {
                    val pieEntries = statisticsVo!!.ratioByDurationOfWeek.map {
                        PieEntry(it.ratio.toFloat(), it.taskName)
                    }

                    val colors = statisticsVo!!.ratioByDurationOfWeek.map {
                        ColorUtil.getColorByRgb(null)
                    }

                    dataDateTxt!!.text = DateUtil.curWeek
                    setPieChartData(pieEntries, colors)
                    pieChart!!.notifyDataSetChanged()
                }

                R.id.record_fragment_time_data_month -> {
                    val pieEntries = statisticsVo!!.ratioByDurationOfMonth.map {
                        PieEntry(it.ratio.toFloat(), it.taskName)
                    }

                    val colors = statisticsVo!!.ratioByDurationOfMonth.map {
                        ColorUtil.getColorByRgb(null)
                    }

                    dataDateTxt!!.text = DateUtil.curMonth
                    setPieChartData(pieEntries, colors)
                    pieChart!!.notifyDataSetChanged()
                }
            }
        }

        // App前台使用时长分布
        // 分享按钮
        binding.appUsedTimeShare.setOnClickListener {

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statisticsVo?.let {
            setCumulativeFocus()
            setTodayFocus()
            setFocusDurationRatio()
        }

        //当前日期：
        todayFocus!!.text = DateUtil.curDay
        dataDateTxt!!.text = DateUtil.curDay
        appDateTxt!!.text = DateUtil.curDay


        //设置圆大小:
        pieChart!!.holeRadius = 50f
        //        pieChart.setHoleColor(Color.RED);//中间空心圆的颜色
        pieChart!!.transparentCircleRadius = 60f

        //        pieChart.setCenterTextRadiusPercent(20);

        //设置饼状图主题：
        pieChart!!.description.isEnabled = false

        val barDataSet = BarDataSet(appTimeAndHead, "App前台使用时间")
        val barData = BarData(barDataSet)
        barData.barWidth = 0.5f //设置条形柱的宽度
        barDataSet.barBorderWidth = 1f //设置条形图边框宽度
        barDataSet.setDrawIcons(true)
        barDataSet.valueTextSize = 10f //设置条形图之上的文字的大小
        barDataSet.valueTextColor = Color.BLUE //设置条形图之上的文字的颜色
        barDataSet.color = Color.RED //设置条形柱子的颜色

        //        barDataSet.setFormLineWidth(100f);
//        barDataSet.setFormSize(10f);
        barHor!!.data = barData

        barHor!!.description.isEnabled = false //隐藏右下角英文
        barHor!!.xAxis.position = XAxis.XAxisPosition.BOTTOM //X轴的位置 默认为右边
        barHor!!.axisLeft.isEnabled = false //隐藏上侧Y轴   默认是上下两侧都有Y轴

        barHor!!.canScrollVertically(1)

        //        barHor.getAxisRight().setDrawGridLines(false);  //是否绘制X轴上的网格线（背景里面的竖线）
//        barHor.getXAxis().setDrawGridLines(false);  //是否绘制Y轴上的网格线（背景里面的横线）
        barHor!!.axisLeft.setDrawGridLines(false)

        barHor!!.xAxis.isEnabled = false //取消X轴数值
        barHor!!.axisRight.isEnabled = false //取消下方的数值
        barHor!!.axisLeft.isEnabled = true //显示上方的数值

        //Y轴自定义坐标
        barHor!!.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                return ""
            }
        }
        barHor!!.axisLeft.axisMaximum = 2000f //Y轴最大数值
        barHor!!.axisLeft.axisMinimum = 0f //Y轴最小数值

        //设置动画
        barHor!!.animateY(1000, Easing.Linear)
        barHor!!.animateX(1000, Easing.Linear)

        barHor!!.isDoubleTapToZoomEnabled = false
        //禁止拖拽
//        barHor.setDragEnabled(false);
        //X轴或Y轴禁止缩放
        barHor!!.isScaleXEnabled = false
        barHor!!.isScaleYEnabled = false
        barHor!!.setScaleEnabled(false)
    }

    private fun setFocusDurationRatio() {
        val pieEntries = statisticsVo!!.ratioByDurationOfDay.map {
            PieEntry(it.ratio.toFloat(), it.taskName)
        }

        val colors = statisticsVo!!.ratioByDurationOfDay.map {
            ColorUtil.getColorByRgb(null)
        }

        setPieChartData(pieEntries, colors)
    }

    /**
     * 设置今日专注的数据
     */
    private fun setTodayFocus() {
        val todayMillisecond = DateUtil.epochMillisecond()
        binding.todayTomatoTimes.text = (statisticsVo!!
            .dayTomatoMap[todayMillisecond]
            ?.tomatoTimes ?: 0).toString()

        binding.todayTomatoDuration.text = (statisticsVo!!
            .dayTomatoMap[todayMillisecond]
            ?.tomatoDuration ?: 0).toString()
    }

    /**
     * 设置累计专注的数据
     */
    private fun setCumulativeFocus() {
        binding.tomatoTimes.text = (statisticsVo!!.tomatoTimes ?: 0).toString()
        binding.tomatoDuration.text = (statisticsVo!!.tomatoDuration ?: 0).toString()
        binding.avgTomatoDuration.text = (statisticsVo!!.avgTomatoTimes ?: 0).toString()
    }

    private val appTimeAndHead: List<BarEntry>
        get() {
            XXPermissions.with(this) // 申请单个权限
                .permission(Permission.PACKAGE_USAGE_STATS) // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                //.unchecked()
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: List<String>, allGranted: Boolean) {
                        if (!allGranted) {
                            Toast.makeText(
                                context,
                                "获取部分权限成功，但部分权限未正常授予",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                        //                        Toast.makeText(getContext(), "获取权限成功", Toast.LENGTH_SHORT).show();
                    }

                    override fun onDenied(permissions: List<String>, doNotAskAgain: Boolean) {
                        if (doNotAskAgain) {
                            Toast.makeText(context, "被拒绝授权，请手动授予权限", Toast.LENGTH_SHORT)
                                .show()
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(context!!, permissions)
                        } else {
                            Toast.makeText(context, "获取权限失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                })


            val usageStatsManager =
                requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val packageManager = requireContext().packageManager

            // 获取当前时间的毫秒数
            val currentTime = System.currentTimeMillis()

            // 获取前一小时内的应用使用情况（可根据需求自定义时间范围）
            val usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - 60 * 60 * 1000,
                currentTime
            )

            // 用于存储应用程序包名与使用时间的映射关系
            val appUsageMap: SortedMap<String, Long> = TreeMap()

            for (usageStats in usageStatsList) {
                val packageName = usageStats.packageName
                val totalTimeInForeground = usageStats.totalTimeInForeground / 1000 // 转换为秒

                if (totalTimeInForeground > 0) {
                    appUsageMap[packageName] = totalTimeInForeground
                }
            }

            // 获取应用程序的头像并打印输出
            for (packageName in appUsageMap.keys) {
                try {
                    val appInfo =
                        packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    val appName = packageManager.getApplicationLabel(appInfo) as String
                    val appIcon = packageManager.getApplicationIcon(appInfo)

                    // 打印应用程序的名称、头像和使用时间
                    println("App Name: $appName")
                    println("App Icon: $appIcon")
                    //          list.add(new BarEntry(5,2,setImageSizeAndDistance("person1")));
                    if (appUsageMap[packageName]!! / 60 != 0L) {
                        val time = (appUsageMap[packageName]!! / 60).toFloat()
                        list.add(BarEntry(i++.toFloat(), time, setImageSizeAndDistance(appIcon)))
                    }
                } catch (e: NameNotFoundException) {
                    e.printStackTrace()
                }
            }
            i = 0
            return list
        }


    private fun setPieChartData(yVals: List<PieEntry>, colors: List<Int>) {
        val pieDataSet = PieDataSet(yVals, "")
        pieDataSet.colors = colors
        pieChart!!.setEntryLabelColor(Color.parseColor("#ff8c00")) //描述文字的颜色
        pieDataSet.valueTextSize = 15f //数字大小
        pieDataSet.valueTextColor = Color.BLACK //数字颜色
        pieDataSet.valueFormatter = object : ValueFormatter() {
            @SuppressLint("DefaultLocale")
            override fun getFormattedValue(value: Float): String {
                return String.format("%.2f%%", value * 100)
            }
        }

        //设置描述的位置
        pieDataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        pieDataSet.valueLinePart1Length = 0.6f //设置描述连接线长度

        //设置数据的位置
        pieDataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        pieDataSet.valueLinePart2Length = 0.6f //设置数据连接线长度

        //设置两根连接线的颜色
        pieDataSet.valueLineColor = Color.BLUE

        val pieData = PieData(pieDataSet)
        pieChart!!.data = pieData
        pieChart!!.setExtraOffsets(0f, 32f, 0f, 32f)
        //动画（如果使用了动画可以则省去更新数据的那一步）
//        pieChart.animateY(1000); //在Y轴的动画  参数是动画执行时间 毫秒为单位
//        pieChart.animateX(1000); //X轴动画
        pieChart!!.animateXY(1000, 1000) //XY两轴混合动画
    }

    private fun setImageSizeAndDistance(drawable: Drawable): Drawable {

        val bitmap: Bitmap
        if (drawable is BitmapDrawable) {
            bitmap = drawable.bitmap
        } else {
            bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }

        // 缩放比例
        val scale = 0.3f // 缩放比例

        // 缩放后的宽高
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()

        // 缩放Bitmap对象
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        // 将缩放后的Bitmap对象转换为Drawable
        val scaledDrawable: Drawable = BitmapDrawable(resources, scaledBitmap)
        scaledDrawable.setBounds(100, 0, 0, 0)

        return scaledDrawable
    }

    fun setExtraOffsets(left: Float, top: Float, right: Float, bottom: Float) {
        pieChart!!.extraLeftOffset = left
        pieChart!!.extraTopOffset = top
        pieChart!!.extraRightOffset = right
        pieChart!!.extraBottomOffset = bottom
    }

    private fun findViews(view: View) {
        pieChart = view.findViewById(R.id.record_fragment_pie_chart)
        barHor = view.findViewById(R.id.record_fragment_bar_chart)
        radioDataGroup = view.findViewById(R.id.record_fragment_time_data)
        radioDayButton = view.findViewById(R.id.record_fragment_time_data_day)
        radioWeekButton = view.findViewById(R.id.record_fragment_time_data_week)
        radioMonthButton = view.findViewById(R.id.record_fragment_time_data_month)

        todayFocus = view.findViewById(R.id.record_fragment_today_focus)
        dataDateTxt = view.findViewById(R.id.record_fragment_time_data_date)
        appDateTxt = view.findViewById(R.id.record_fragment_app_use_time_txt)
    }
}
