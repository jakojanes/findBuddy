import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class FindUsersWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val db = FirebaseFirestore.getInstance()
        val userA = inputData.getDouble("lat", 0.0)
        val userB = inputData.getDouble("lng", 0.0)
        val center = GeoLocation(userA, userB)
        val radiusInKm = inputData.getDouble("radiusInKm", 50.0)


        val maximumRetrieved = inputData.getInt("maximumRetrieved", 30)

        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInKm * 1000)
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        for (b in bounds) {
            val q = db.collection("users")
                .orderBy("location")
                .startAt(b.startHash)
                .endAt(b.endHash)
                .limit(maximumRetrieved.toLong())
            tasks.add(q.get())
        }

        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                val matchingUsers: MutableList<DocumentSnapshot> = ArrayList()
                for (task in tasks) {
                    val snap = task.result
                    for (doc in snap!!.documents) {
                        val lat = doc.getDouble("lat")!!
                        val lng = doc.getDouble("lng")!!

                        val docLocation = GeoLocation(lat, lng)
                        val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                        if (distanceInM <= radiusInKm * 1000) {
                            matchingUsers.add(doc)
                        }
                    }
                }

                for (doc in matchingUsers) {
                    Log.e("Matching document: ", doc.data.toString())
                }
            }

        return Result.success()
    }
}
