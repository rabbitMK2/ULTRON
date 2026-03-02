package com.ai.assistance.operit.ui.features.ultron.roubao.wikipedia

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * 维基百科API客户端
 * 优先使用国内可直连的镜像网关，无需VPN即可访问
 * 镜像网关：https://wikipedia.zyhorg.cn
 * 备用：官方API https://zh.wikipedia.org/w/api.php（国内可能无法访问）
 */
class WikipediaClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // 使用国内可直连的镜像网关（优先）
    private val baseUrl = "https://wikipedia.zyhorg.cn/w/api.php"
    
    // 备用官方API（如果镜像失效可以切换）
    // private val baseUrl = "https://zh.wikipedia.org/w/api.php"

    /**
     * 搜索维基百科页面并获取摘要
     * @param query 查询关键词
     * @return Result<String> 成功返回格式化后的内容，失败返回错误信息
     */
    suspend fun search(query: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 先搜索页面标题
            val searchResult = searchTitle(query)
            if (searchResult.isFailure) {
                return@withContext Result.failure(searchResult.exceptionOrNull() ?: Exception("搜索失败"))
            }

            val title = searchResult.getOrNull() ?: return@withContext Result.failure(Exception("未找到相关页面"))
            
            // 获取页面内容
            val contentResult = getPageContent(title)
            if (contentResult.isFailure) {
                return@withContext Result.failure(contentResult.exceptionOrNull() ?: Exception("获取内容失败"))
            }

            val content = contentResult.getOrNull() ?: ""
            if (content.isEmpty()) {
                return@withContext Result.failure(Exception("页面内容为空"))
            }

            Result.success(formatContent(title, content))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 搜索页面标题
     */
    private suspend fun searchTitle(query: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$baseUrl?action=query&list=search&srsearch=$encodedQuery&srlimit=1&format=json"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("响应体为空"))
            val json = JSONObject(responseBody)
            
            val queryObj = json.optJSONObject("query")
            val searchArray = queryObj?.optJSONArray("search")
            
            if (searchArray == null || searchArray.length() == 0) {
                return@withContext Result.failure(Exception("未找到相关内容"))
            }

            val firstResult = searchArray.getJSONObject(0)
            val title = firstResult.getString("title")
            
            Result.success(title)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取页面内容（摘要）
     */
    private suspend fun getPageContent(title: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            // 使用extracts参数获取摘要，exintro表示只获取引言部分，exchars限制字符数
            val url = "$baseUrl?action=query&titles=$encodedTitle&prop=extracts&exintro&exchars=800&explaintext&format=json"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("响应体为空"))
            val json = JSONObject(responseBody)
            
            val queryObj = json.optJSONObject("query")
            val pagesObj = queryObj?.optJSONObject("pages")
            
            if (pagesObj == null) {
                return@withContext Result.failure(Exception("无效的响应格式"))
            }

            // 获取第一个页面（通常只有一个）
            val pageId = pagesObj.keys().next()
            val pageObj = pagesObj.getJSONObject(pageId)
            
            // 检查页面是否存在
            if (pageObj.has("missing")) {
                return@withContext Result.failure(Exception("页面不存在"))
            }

            val extract = pageObj.optString("extract", "")
            
            if (extract.isEmpty()) {
                return@withContext Result.failure(Exception("页面内容为空"))
            }

            Result.success(extract)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 格式化内容为友好的显示格式
     */
    private fun formatContent(title: String, content: String): String {
        val cleanedContent = content.trim()
            .replace(Regex("\\n\\n+"), "\n\n") // 合并多个换行
            .trim()

        return buildString {
            append("📖 $title\n\n")
            append(cleanedContent)
            
            // 如果内容被截断，添加提示
            if (cleanedContent.length >= 780) {
                append("\n\n")
                append("💡 提示：以上为摘要信息。如需查看完整内容，请访问中文维基百科。")
            }
        }
    }
}

