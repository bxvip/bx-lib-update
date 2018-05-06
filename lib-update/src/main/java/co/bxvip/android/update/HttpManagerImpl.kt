package co.bxvip.android.update

import android.accounts.NetworkErrorException
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import okhttp3.*
import org.json.JSONException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.io.*


/**
 * <pre>
 *     author: vic
 *     time  : 18-4-10
 *     desc  : 网络启动器，针对apk下载，插件下载
 * </pre>
 */
class HttpManagerImpl : HttpManager {
    override fun asyncGet(url: String, params: MutableMap<String, String>, callBack: HttpManager.Callback) {

        doNet(url, params, callBack)
    }

    private fun paramsMap2String(params: MutableMap<String, String>): String {
        return if (params.isEmpty())
            ""
        else {
            var res = ""
            params.map {
                res += "${it.key}=${it.value}&"
            }
            if (res.length > 1) {
                res = res.substring(0, res.length - 1)
            }
            println(res)
            res
        }
    }

    override fun asyncPost(url: String, params: MutableMap<String, String>, callBack: HttpManager.Callback) {
        doNet(url, params, callBack)
    }

    override fun download(url: String, path: String, fileName: String, callback: HttpManager.FileCallback) {
        FileDownloader.getImpl().create(url)
                .setPath(File(path, fileName).absolutePath, false)
                .setCallbackProgressTimes(300)
                .setMinIntervalUpdateSpeed(400)
                .setListener(object : FileDownloadListener() {
                    override fun warn(task: BaseDownloadTask?) {

                    }

                    override fun completed(task: BaseDownloadTask?) {
                        callback.onResponse(File(task!!.path))
                    }

                    override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {

                    }

                    override fun error(task: BaseDownloadTask?, e: Throwable?) {
                        callback.onError(validateError(e, 0))
                    }

                    override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                        callback.onProgress(soFarBytes.toFloat(), totalBytes.toLong())
                    }

                    override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {

                    }

                }).start()
    }

    private var sOkClient: OkHttpClient? = null


    fun doNet(url: String, params: MutableMap<String, String>, callBack: HttpManager.Callback?, method: String = "GET") {
        try {
            val request = if (!"GET".equals(method, ignoreCase = true)) {// POST请求
                val bodyBuilder = FormBody.Builder()

                params.map {
                    bodyBuilder.add(it.key, it.value)
                }

                Request.Builder()
                        .url(url)
                        .post(bodyBuilder.build())
                        .build()
            } else {// GET请求
                val newUrl = url + "?" + paramsMap2String(params)
                Request.Builder()
                        .url(newUrl)
                        .method(method, null)
                        .build()
            }

            if (sOkClient == null) {
                sOkClient = OkHttpClient()
            }

            sOkClient?.newCall(request)?.enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    callBack?.onError(validateError(e, 0))
                }

                override fun onResponse(call: Call?, response: Response?) {
                    if (response?.isSuccessful!!) {
                        callBack?.onResponse(response.body()?.string()!!)
                    } else {
                        callBack?.onError(validateError(null, response.code()))
                    }
                }

            })
        } catch (e: Exception) {
            callBack?.onError(validateError(e, 0))
        } finally {
            sOkClient = null
        }
    }


    private fun validateError(error: Throwable?, code: Int): String {
        if (error != null) {
            when (error) {
                is NetworkErrorException -> return "网络异常，请联网重试"
                is SocketTimeoutException -> return "网络连接超时，请稍候重试"
                is JSONException -> return "json转化异常"
                is ConnectException -> return "服务器网络异常或宕机，请稍候重试"
                else -> {
                }
            }
        }

        if (code > 200) {
            return when {
                code >= 500 -> "服务器异常，请稍候重试"
                code in 400..499 -> "接口异常，请稍候重试"
                else -> String.format("未知异常 code = %d，请稍候重试", code)
            }
        }
        return "未知异常，请稍候重试"
    }
}