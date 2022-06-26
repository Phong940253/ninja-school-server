import json
from sre_constants import FAILURE, SUCCESS
from typing import Optional
import re
from django.http.response import JsonResponse
from rest_framework.decorators import api_view
from rest_framework.response import Response
from django.shortcuts import render
from rest_framework import status
from django.http import FileResponse
from django.db import transaction
from ratelimit.decorators import ratelimit
from NSOAdmin.enums.card_status_enum import CardStatusEnum
from common import response, secure
from django.http import HttpResponse
from django.core.cache import caches
import logging

from NSOAdmin.models import CardDCoin, Player
from common.base_exception import BaseApiException

logger = logging.getLogger(__name__)

cache = caches["default"]


@api_view(["GET"])
def server_list(request):
    """
    List all code snippets, or create a new snippet.
    """
    if request.method == "GET":
        server_format: str = "Hỏa quốc:localhost:14444:0:0"
        return HttpResponse(server_format,
                            content_type="text/plain; charset=utf8")


@api_view(["GET"])
def srvip(request):
    if request.method == "GET":
        server_format: str = "Hỏa quốc:localhost:14444:0:0"

        return HttpResponse(server_format,
                            content_type="text/plain; charset=utf8")


def index(request):
    return render(request, "index.html")


def downloads(request):
    return render(request, "downloads.html")


# @ratelimit(key="ip", rate="3/365d", block=True)
@api_view(["POST"])
@transaction.atomic
def register(request):
    client_ip: str = secure.get_ip_from_request(request)

    if request.method == "POST":
        username: Optional[str] = request.data.get("user")
        password: Optional[str] = request.data.get("pass")

        if not (username and password):
            return response.fail("Tài khoản và mật khẩu không được trống.")

        if not (
            username == re.findall(r"([a-z0-9]+)", username)[0]
            and password == re.findall(r"([a-z0-9]+)", password)[0]
        ):
            return response.fail(
                "Tài khoản và mật khẩu phải là số và chữ thường.")

        if len(username) < 8 or len(password) < 8:
            return response.fail(
                "Tài khoản hoặc mật khẩu phải từ 8 kí tự trở lên.")

        if password.isdigit() or password.isalpha():
            return response.fail("Mật khẩu cần phải có cả chữ thường và số.")

        if (
            ("123" in password)
            or ("abc" in password)
            or (len(list(set(password))) <= 2)
        ):
            return response.fail(
                "Mật khẩu không an toàn. Vui lòng chọn mật khẩu khác.")

        player: Optional[Player] = Player.objects.filter(
            username=username).first()
        if not player:
            num_regis: int = cache.get_or_set(
                client_ip, 0, timeout=86400 * 365)

            status: str = "wait"
            if num_regis >= 2:
                status = "block"

            Player.objects.create(
                username=username,
                password=password,
                luong=0,
                status=status,
                ip=client_ip,
            )
            cache.incr(client_ip)
        else:
            raise BaseApiException(
                f"Bạn đã đăng kí tài khoàn thất bại. Username: {username} đã tồn tại."
            )

        return response.success(
            {"status": "Success", "message": "Bạn đã đăng kí tài khoàn thành công."}
        )


@api_view(["GET"])
@transaction.atomic
def topup_card_webhook(request, *args, **kwargs):
    if request.method == "GET":
        logger.info("Data webhook: " + json.dumps(request.GET))

        try:
            value_send = request.GET.get("value_send")
            request_id = request.GET.get("request_id")
            value_real = request.GET.get("value_real")
            guest_receive = request.GET.get("guest_receive")
            _status = int(request.GET.get("status"))

            card: CardDCoin = CardDCoin.objects.get(
                request_id=request_id, status=int(CardStatusEnum.IN_PROGRESS)
            )
            if _status == 200:
                card.status = int(CardStatusEnum.SUCCESS)
            elif _status == 201:
                card.status = int(CardStatusEnum.WRONG_VALUE)
            else:
                card.status = int(CardStatusEnum.FAILURE)

            card.save()
        except Exception as ex:
            logger.info("ERROR: " + str(ex))
            return response.fail("Failure: " + str(ex))

        return response.success({"status": "Success"})
