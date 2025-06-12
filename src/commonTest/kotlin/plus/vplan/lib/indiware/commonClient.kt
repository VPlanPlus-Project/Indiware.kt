package plus.vplan.lib.indiware

import plus.vplan.lib.indiware.source.IndiwareClient


internal val commonIndiwareClient = getClient()

expect fun getClient(): IndiwareClient