package org.theta.delivery.sample

import org.theta.deliverysdk.models.ThetaDeliveryEvent

import java.util.Date

class ThetaEventWithDate internal constructor(val event: ThetaDeliveryEvent, val date: Date)
