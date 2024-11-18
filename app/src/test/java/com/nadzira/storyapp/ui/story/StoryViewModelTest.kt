package com.nadzira.storyapp.ui.story

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.nadzira.storyapp.DataDummy
import com.nadzira.storyapp.MainDispatcherRule
import com.nadzira.storyapp.getOrAwaitValue
import com.nadzira.storyapp.remote.response.StoryEntity
import com.nadzira.storyapp.ui.Repository
import com.nadzira.storyapp.ui.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var repository: Repository

    private lateinit var storyViewModel: StoryViewModel

    @Before
    fun setUp() {
        storyViewModel = StoryViewModel(repository)
    }

    @Test
    fun `when Get Stories Should Not Be Null and Return Data`() = runTest {
        val dummyStory = DataDummy.generateDummyStoryEntity()
        val pagingData = StoryPagingSource.snapshot(dummyStory)
        val expectedStory = MutableLiveData<Result<PagingData<StoryEntity>>>()
        expectedStory.value = Result.Success(pagingData)
        `when`(repository.getStories()).thenReturn(expectedStory)

        val actualStory = storyViewModel.getStories().getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData((actualStory as Result.Success).data)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStory.size, differ.snapshot().size)
        Assert.assertEquals(dummyStory[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Stories Empty Should Return No Data`() = runTest {
        val pagingData = PagingData.from(emptyList<StoryEntity>())
        val expectedStory = MutableLiveData<Result<PagingData<StoryEntity>>>()
        expectedStory.value = Result.Success(pagingData)
        `when`(repository.getStories()).thenReturn(expectedStory)

        val actualStory = storyViewModel.getStories().getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData((actualStory as Result.Success).data)

        Assert.assertEquals(0, differ.snapshot().size)
    }
}

class StoryPagingSource : PagingSource<Int, StoryEntity>() {
    companion object {
        fun snapshot(items: List<StoryEntity>): PagingData<StoryEntity> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoryEntity>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryEntity> {
        return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
