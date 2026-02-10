import {createRouter, createWebHistory} from 'vue-router'
import HomePage from '../views/HomePage.vue'
import ProblemList from '../views/ProblemList.vue'
import ProblemDetail from '../views/ProblemDetail.vue'
import SubmitResult from '../views/SubmitResult.vue'
import RankingPage from '../views/RankingPage.vue'
import DiscussionPage from '../views/DiscussionPage.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomePage
  },
  {
    path: '/problems',
    name: 'ProblemList',
    component: ProblemList
  },
  {
    path: '/problem/:id',
    name: 'ProblemDetail',
    component: ProblemDetail
  },
  {
    path: '/submit/:id',
    name: 'SubmitResult',
    component: SubmitResult
  },
  {
    path: '/ranking',
    name: 'Ranking',
    component: RankingPage
  },
  {
    path: '/discussion',
    name: 'Discussion',
    component: DiscussionPage
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router