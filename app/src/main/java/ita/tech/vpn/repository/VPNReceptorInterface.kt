package ita.tech.vpn.repository

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ita.tech.vpn.dataStore.StoreVPN

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VPNReceptorInterface {
    fun getStoreVPN(): StoreVPN
}