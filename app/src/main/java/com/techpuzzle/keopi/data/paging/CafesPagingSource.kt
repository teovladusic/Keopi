package com.techpuzzle.keopi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.keopiApp
import com.techpuzzle.keopi.utils.bsonToCafeBar
import com.techpuzzle.keopi.utils.seed
import io.realm.mongodb.mongo.iterable.MongoCursor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bson.Document
import java.lang.Exception

class CafesPagingSource(
    private val searchQuery: String,
    private val matchQueries: List<Document>
) : PagingSource<MongoCursor<Document>, CafeBar>() {

    private var documentsSize = 0

    private val collection =
        keopiApp.currentUser()!!.getMongoClient("mongodb-atlas").getDatabase("KeopiDatabase")
            .getCollection("cafes")

    override fun getRefreshKey(state: PagingState<MongoCursor<Document>, CafeBar>): MongoCursor<Document>? =
        null

    override suspend fun load(params: LoadParams<MongoCursor<Document>>): LoadResult<MongoCursor<Document>, CafeBar> {
        var data: MutableList<CafeBar>? = mutableListOf()
        var nextPage: MongoCursor<Document>? = null
        CoroutineScope(Dispatchers.IO).launch {
            val currentPage = params.key ?: getDocuments()

            nextPage = getDocuments()

            currentPage?.forEach { document ->
                try {
                    val cafeBarDoc = collection.findOne(Document("_id", document["_id"])).get()
                    data?.add(bsonToCafeBar(cafeBarDoc)!!)
                } catch (e: Exception) {
                    data = null
                }

            }
        }.join()

        return try {
            LoadResult.Page(
                data = data ?: emptyList(),
                nextKey = if (nextPage?.iterator()?.hasNext() == true) nextPage else null,
                prevKey = null
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    private suspend fun getDocuments(): MongoCursor<Document>? {
        var documentIds: MongoCursor<Document>? = null
        CoroutineScope(Dispatchers.IO).launch {
            val pipeline = mutableListOf<Document>()
            if (searchQuery.isNotEmpty()) pipeline.add(
                Document(
                    "\$search",
                    Document("text", Document("query", searchQuery).append("path", "name").append("fuzzy", Document("maxEdits", 2)))
                )
            )

            if (matchQueries.isNotEmpty()) {
                pipeline.addAll(matchQueries)
            }

            pipeline.add(
                Document(
                    "\$project",
                    Document("remainderValue", Document("\$mod", listOf("\$seed", seed)))
                )
            )
            pipeline.add(Document("\$sort", Document("remainderValue", 1).append("_id", 1)))
            pipeline.add(Document("\$skip", documentsSize))
            pipeline.add(Document("\$limit", 5))

            documentsSize += 5
            documentIds = try {
                collection.aggregate(pipeline).iterator().get()
            }catch (e: Exception) {
                null
            }

        }.join()
        return documentIds
    }
}