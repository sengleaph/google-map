package com.sifu.mylocation.presentation.state

sealed class BranchIntent {
    object LoadBranches : BranchIntent()
}